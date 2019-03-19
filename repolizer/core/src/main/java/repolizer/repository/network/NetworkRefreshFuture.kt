package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.persistent.CacheItem
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkRefreshFuture
constructor(repolizer: Repolizer,
            futureRequest: NetworkFutureRequest) : NetworkFuture<Boolean>(repolizer, futureRequest) {

    private val fetchSecurityLayer: FetchSecurityLayer = futureRequest.fetchSecurityLayer
    private val allowMultipleRequestsAtSameTime: Boolean = futureRequest.allowMultipleRequestsAtSameTime

    private val insertSql: String = futureRequest.insertSql

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, wrapperType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onDetermineExecutionType(): ExecutionType {
        return if (allowMultipleRequestsAtSameTime || fetchSecurityLayer.allowFetch()) {
            ExecutionType.USE_NETWORK
        } else ExecutionType.DO_NOTHING
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        return when (executionType) {
            ExecutionType.USE_NETWORK -> fetchFromNetwork()
            else -> null
        }
    }

    private fun fetchFromNetwork(): Boolean? {
        val response: NetworkResponse<String>? = networkAdapter?.execute(this, requestProvider)

        return if (response?.isSuccessful() == true && response.body != null) {
            val saveSuccessful = storageAdapter?.insert(repositoryClass, converterAdapter, fullUrl,
                    insertSql, response.body, bodyType)
            if (saveSuccessful == true) {
                val successfullyCached = cacheAdapter?.save(repositoryClass, CacheItem(fullUrl, System.currentTimeMillis()))
                if (successfullyCached == true) {
                    repolizer.defaultMainThread.execute {
                        responseService?.handleSuccess(requestType, futureRequest)
                    }
                    true
                } else {
                    repolizer.defaultMainThread.execute {
                        responseService?.handleCacheError(requestType, futureRequest)
                    }
                    false
                }
            } else {
                repolizer.defaultMainThread.execute {
                    responseService?.handleStorageError(requestType, futureRequest)
                }
                false
            }
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleRequestError(requestType, futureRequest, response)
            }
            false
        }
    }

    override fun onFinished(result: Boolean?) {
        super.onFinished(result)
        fetchSecurityLayer.onFetchFinished()
    }
}
