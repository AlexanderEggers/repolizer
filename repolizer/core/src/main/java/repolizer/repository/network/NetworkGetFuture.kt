package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState
import repolizer.repository.response.NetworkResponse
import repolizer.adapter.util.AdapterUtil

class NetworkGetFuture<Body>
constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder) : NetworkFuture<Body>(repolizer, futureBuilder) {

    private val deleteIfCacheIsTooOld: Boolean = futureBuilder.isDeletingCacheIfTooOld
    private var allowFetch: Boolean = futureBuilder.allowFetch
    private val allowMultipleRequestsAtSameTime: Boolean = futureBuilder.allowMultipleRequestsAtSameTime

    private val freshCacheTime = futureBuilder.freshCacheTime
    private val maxCacheTime = futureBuilder.maxCacheTime

    private val insertSql: String = futureBuilder.insertSql
    private val querySql: String = futureBuilder.querySql
    private val deleteSql: String = futureBuilder.deleteSql

    private var fetchSecurityLayer: FetchSecurityLayer = futureBuilder.fetchSecurityLayer
            ?: throw IllegalStateException("FetchSecurityLayer is null.")

    private lateinit var cacheState: CacheState

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, bodyType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>

        return if(wrapperAdapter.canHaveStorageConnection()
                && storageAdapter?.canHaveActiveConnection() == true) {
            storageAdapter.establishConnection(repositoryClass, fullUrl, querySql)
                    ?: throw IllegalStateException("If you want to use an active storage connection, " +
                            "you need to implement the establishConnection() function inside your " +
                            "StorageAdapter.")
        } else wrapperAdapter.execute(this)
    }

    override fun onDetermineExecutionType(): ExecutionType {
        this.cacheState = cacheAdapter?.get(repositoryClass, fullUrl, freshCacheTime, maxCacheTime)
                ?: CacheState.NEEDS_NO_REFRESH

        val cacheData = storageAdapter?.get(repositoryClass, fullUrl, querySql)
        val needsFetch = cacheState == CacheState.NEEDS_SOFT_REFRESH ||
                cacheState == CacheState.NEEDS_HARD_REFRESH || cacheState == CacheState.NO_CACHE

        return if ((cacheData == null || needsFetch) && allowFetch) {
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

    override fun onFinished() {
        super.onFinished()
        fetchSecurityLayer.onFetchFinished()
    }

    private fun fetchFromNetwork(): Body? {
        val response: NetworkResponse<String> = networkAdapter.execute(this, requestProvider)

        return if (response.isSuccessful() && response.body != null) {
            if(saveData) {
                val saveSuccessful = storageAdapter?.insert(repositoryClass, fullUrl, insertSql,
                        response.body, bodyType.rawType)
                if(saveSuccessful == true) {
                    responseService?.handleSuccess(requestType, response)
                    cacheAdapter?.save(repositoryClass, CacheItem(fullUrl, System.currentTimeMillis()))
                    storageAdapter?.get(repositoryClass, fullUrl, querySql)
                } else {
                    responseService?.handleStorageError(requestType, response)
                    null
                }
            } else {
                converterAdapter.convertStringToData(repositoryClass, response.body, bodyType.rawType) as? Body
            }
        } else {
            responseService?.handleRequestError(requestType, response)
            if (deleteIfCacheIsTooOld && cacheState == CacheState.NEEDS_HARD_REFRESH) {
                storageAdapter?.delete(repositoryClass, fullUrl, deleteSql)
            }
            null
        }
    }

    private fun fetchCacheData(): Body? {
        return storageAdapter?.get(repositoryClass, fullUrl, querySql)
    }
}
