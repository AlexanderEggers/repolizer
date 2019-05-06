package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.persistent.CacheItem
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkRefreshFuture
constructor(private val repolizer: Repolizer,
            private val futureRequest: NetworkFutureRequest) : NetworkFuture<Boolean>(repolizer, futureRequest) {

    private val fetchSecurityLayer: FetchSecurityLayer = futureRequest.fetchSecurityLayer
    private val allowMultipleRequestsAtSameTime: Boolean = futureRequest.allowMultipleRequestsAtSameTime

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, futureRequest.typeToken.type,
                futureRequest.repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this, futureRequest)
                ?: throw IllegalStateException("It seems like that your WrapperAdapter does not" +
                        "have the method execute() implemented.")
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
        val response: NetworkResponse<String>? = networkAdapter?.execute(futureRequest, requestProvider)

        return if (response?.isSuccessful() == true && response.body != null) {
            val saveSuccessful = storageAdapter?.insert(futureRequest, converterAdapter, response.body)
            if (saveSuccessful == true) {
                val successfullyCached = cacheAdapter?.save(futureRequest, CacheItem(futureRequest.fullUrl))
                if (successfullyCached == true) {
                    repolizer.defaultMainThread.execute {
                        responseService?.handleSuccess(futureRequest)
                    }
                    true
                } else {
                    repolizer.defaultMainThread.execute {
                        responseService?.handleCacheError(futureRequest)
                    }
                    false
                }
            } else {
                repolizer.defaultMainThread.execute {
                    responseService?.handleStorageError(futureRequest)
                }
                false
            }
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleRequestError(futureRequest, response)
            }
            false
        }
    }

    override fun onFinished(result: Boolean?) {
        super.onFinished(result)
        fetchSecurityLayer.onFetchFinished()
    }
}
