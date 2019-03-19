package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkGetFuture<Body>
constructor(repolizer: Repolizer,
            futureRequest: NetworkFutureRequest) : NetworkFuture<Body>(repolizer, futureRequest) {

    private val deleteIfCacheIsTooOld: Boolean = futureRequest.isDeletingCacheIfTooOld
    private var allowFetch: Boolean = futureRequest.allowFetch
    private val allowMultipleRequestsAtSameTime: Boolean = futureRequest.allowMultipleRequestsAtSameTime

    private val freshCacheTime = futureRequest.freshCacheTime
    private val maxCacheTime = futureRequest.maxCacheTime

    private val insertSql: String = futureRequest.insertSql
    private val querySql: String = futureRequest.querySql
    private val deleteSql: String = futureRequest.deleteSql

    private var fetchSecurityLayer: FetchSecurityLayer = futureRequest.fetchSecurityLayer

    private var wrapperCanHaveActiveConnection: Boolean = false
    private lateinit var cacheState: CacheState

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, wrapperType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        wrapperCanHaveActiveConnection = wrapperAdapter.canHaveStorageConnection()

        return if (wrapperAdapter.canHaveStorageConnection()
                && storageAdapter?.canHaveActiveConnections() == true) {
            wrapperAdapter.execute(this, storageAdapter, repositoryClass, fullUrl, querySql)
                    ?: throw IllegalStateException("If you want to use an active storage connection, " +
                            "you need to implement the non-default execute() of your WrapperAdapter " +
                            "and establishConnection() function inside your StorageAdapter.")
        } else wrapperAdapter.execute(this)
    }

    override fun onDetermineExecutionType(): ExecutionType {
        this.cacheState = cacheAdapter?.get(repositoryClass, fullUrl, freshCacheTime, maxCacheTime)
                ?: CacheState.NEEDS_NO_REFRESH

        val cacheData = storageAdapter?.get(repositoryClass, converterAdapter, fullUrl, querySql, bodyType)
        val needsFetch = cacheState == CacheState.NEEDS_SOFT_REFRESH ||
                cacheState == CacheState.NEEDS_HARD_REFRESH || cacheState == CacheState.NO_CACHE

        return if (requestUrl.isNotEmpty() && (cacheData == null || needsFetch) && allowFetch) {
            if (allowMultipleRequestsAtSameTime || fetchSecurityLayer.allowFetch()) {
                ExecutionType.USE_NETWORK
            } else {
                ExecutionType.USE_STORAGE
            }
        } else {
            ExecutionType.USE_STORAGE
        }
    }

    override fun onExecute(executionType: ExecutionType): Body? {
        return when (executionType) {
            ExecutionType.USE_NETWORK -> fetchFromNetwork()
            ExecutionType.USE_STORAGE -> fetchCacheData()
            ExecutionType.DO_NOTHING -> null
        }
    }

    override fun onFinished(result: Body?) {
        super.onFinished(result)
        fetchSecurityLayer.onFetchFinished()
    }

    private fun fetchFromNetwork(): Body? {
        val response: NetworkResponse<String>? = networkAdapter?.execute(this, requestProvider)

        return if (response?.isSuccessful() == true && response.body != null) {
            if (saveData) saveNetworkResponse(response)
            else {
                if (bodyType == String::class.java) {
                    response.body as? Body?
                } else {
                    val data: Body? = converterAdapter?.convertStringToData(repositoryClass, response.body, bodyType)
                    if(data == null) responseService?.handleStorageError(requestType, futureRequest)
                    data
                }
            }
        } else {
            handleRequestError(response)
            null
        }
    }

    private fun saveNetworkResponse(response: NetworkResponse<String>): Body? {
        val saveSuccessful = storageAdapter?.insert(repositoryClass, converterAdapter, fullUrl, insertSql,
                response.body!!, bodyType) ?: false
        return if (saveSuccessful) {
            val cacheSuccessful = cacheAdapter?.save(repositoryClass, CacheItem(fullUrl)) ?: true

            //If no cacheAdapter given, ignore check
            if (cacheSuccessful) {
                repolizer.defaultMainThread.execute {
                    responseService?.handleSuccess(requestType, futureRequest)
                }

                if (!wrapperCanHaveActiveConnection || storageAdapter?.canHaveActiveConnections() == false) {
                    storageAdapter?.get(repositoryClass, converterAdapter, fullUrl, querySql, bodyType)
                } else null
            } else {
                repolizer.defaultMainThread.execute {
                    responseService?.handleCacheError(requestType, futureRequest)
                }
                null
            }
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleStorageError(requestType, futureRequest)
            }
            null
        }
    }

    private fun handleRequestError(response: NetworkResponse<String>?) {
        repolizer.defaultMainThread.execute {
            responseService?.handleRequestError(requestType, futureRequest, response)
        }

        if (deleteIfCacheIsTooOld && cacheState == CacheState.NEEDS_HARD_REFRESH) {
            storageAdapter?.delete(repositoryClass, fullUrl, deleteSql)
            cacheAdapter?.delete(repositoryClass, CacheItem(fullUrl))
        }
    }

    private fun fetchCacheData(): Body? {
        return storageAdapter?.get(repositoryClass, converterAdapter, fullUrl, querySql, bodyType)
    }
}
