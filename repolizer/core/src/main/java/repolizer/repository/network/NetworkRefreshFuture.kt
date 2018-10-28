package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.persistent.CacheItem
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkRefreshFuture
constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder) : NetworkFuture<Boolean>(repolizer, futureBuilder) {

    private val fetchSecurityLayer: FetchSecurityLayer = futureBuilder.fetchSecurityLayer
            ?: throw IllegalStateException("FetchSecurityLayer is null.")
    private val allowMultipleRequestsAtSameTime: Boolean = futureBuilder.allowMultipleRequestsAtSameTime

    private val insertSql: String = futureBuilder.insertSql

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
        val response: NetworkResponse<String> = networkAdapter?.execute(this, requestProvider)
                ?: throw IllegalStateException("Network Adapter error: Your url that you have " +
                        "set inside your repository method is empty.")

        return if (response.isSuccessful() && response.body != null) {
            val saveSuccessful = storageAdapter?.insert(repositoryClass, converterAdapter, fullUrl,
                    insertSql, response.body, bodyType)
            if (saveSuccessful == true) {
                val successfullyCached = cacheAdapter?.save(repositoryClass, CacheItem(fullUrl, System.currentTimeMillis()))
                if (successfullyCached == true) {
                    repolizer.defaultMainThread.execute {
                        responseService?.handleSuccess(requestType, response)
                    }
                    true
                } else {
                    repolizer.defaultMainThread.execute {
                        responseService?.handleCacheError(requestType, response)
                    }
                    false
                }
            } else {
                repolizer.defaultMainThread.execute {
                    responseService?.handleStorageError(requestType, response)
                }
                false
            }
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleRequestError(requestType, response)
            }
            false
        }
    }

    override fun onFinished(result: Boolean?) {
        super.onFinished(result)
        fetchSecurityLayer.onFetchFinished()
    }
}
