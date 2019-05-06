package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkGetFuture<Body>
constructor(private val repolizer: Repolizer,
            private val futureRequest: NetworkFutureRequest) : NetworkFuture<Body>(repolizer, futureRequest) {

    private lateinit var cacheState: CacheState

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters,
                futureRequest.typeToken.type, futureRequest.repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return if (wrapperAdapter.canHaveStorageConnection() && storageAdapter?.canHaveActiveConnections() == true) {
            wrapperAdapter.establishStorageConnection(this, futureRequest, storageAdapter)
                    ?: throw IllegalStateException("If you want to use an active storage connection, " +
                            "you need to implement the method establishStorageConnection() of your " +
                            "WrapperAdapter and establishConnection() function inside your StorageAdapter.")
        } else wrapperAdapter.execute(this, futureRequest)
                ?: throw IllegalStateException("It seems like that your WrapperAdapter does not" +
                        "have the method execute() implemented.")
    }

    override fun onDetermineExecutionType(): ExecutionType {
        this.cacheState = cacheAdapter?.get(futureRequest, futureRequest.fullUrl,
                futureRequest.freshCacheTime, futureRequest.maxCacheTime)
                ?: CacheState.NEEDS_NO_REFRESH

        val cacheData = storageAdapter?.get(futureRequest, converterAdapter)
        val needsFetch = cacheState == CacheState.NEEDS_SOFT_REFRESH ||
                cacheState == CacheState.NEEDS_HARD_REFRESH ||
                cacheState == CacheState.NO_CACHE

        return if (futureRequest.url.isNotEmpty()
                && (cacheData == null || needsFetch)
                && futureRequest.allowFetch
                && (futureRequest.allowMultipleRequestsAtSameTime || futureRequest.fetchSecurityLayer.allowFetch())) {
            ExecutionType.USE_NETWORK
        } else ExecutionType.USE_STORAGE
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
        futureRequest.fetchSecurityLayer.onFetchFinished()
    }

    private fun fetchFromNetwork(): Body? {
        val response: NetworkResponse<String>? = networkAdapter?.execute(futureRequest, requestProvider)

        return if (response?.isSuccessful() == true && response.body != null) {
            if (saveData) saveNetworkResponse(response)
            else {
                if (futureRequest.bodyType == String::class.java) response.body as? Body?
                else {
                    val data: Body? = converterAdapter?.convertStringToData(
                            futureRequest.repositoryClass, response.body, futureRequest.bodyType)
                    if (data == null) responseService?.handleStorageError(futureRequest)
                    data
                }
            }
        } else {
            handleRequestError(response)
            null
        }
    }

    private fun saveNetworkResponse(response: NetworkResponse<String>): Body? {
        val saveSuccessful = storageAdapter?.insert(futureRequest, converterAdapter, response.body)
                ?: false
        return if (saveSuccessful) {
            val cacheSuccessful = cacheAdapter?.save(futureRequest, CacheItem(futureRequest.fullUrl))
                    ?: true

            if (cacheSuccessful) {
                repolizer.defaultMainThread.execute {
                    responseService?.handleSuccess(futureRequest)
                }

                storageAdapter?.get(futureRequest, converterAdapter)
            } else {
                repolizer.defaultMainThread.execute {
                    responseService?.handleCacheError(futureRequest)
                }
                null
            }
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleStorageError(futureRequest)
            }
            null
        }
    }

    private fun handleRequestError(response: NetworkResponse<String>?) {
        repolizer.defaultMainThread.execute {
            responseService?.handleRequestError(futureRequest, response)
        }

        if (futureRequest.isDeletingCacheIfTooOld && cacheState == CacheState.NEEDS_HARD_REFRESH) {
            storageAdapter?.delete(futureRequest)
            cacheAdapter?.delete(futureRequest, CacheItem(futureRequest.fullUrl))
        }
    }

    private fun fetchCacheData(): Body? {
        return storageAdapter?.get(futureRequest, converterAdapter)
    }
}
