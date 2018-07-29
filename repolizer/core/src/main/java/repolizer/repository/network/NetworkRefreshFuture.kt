package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.persistent.CacheItem
import repolizer.repository.response.NetworkResponse

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
        val response: NetworkResponse<String> = networkAdapter.execute(this, requestProvider)

        return if (response.isSuccessful() && response.body != null) {
            val saveSuccessful = storageAdapter?.insert(repositoryClass, fullUrl, insertSql,
                    response.body)
            if (saveSuccessful == true) {
                responseService?.handleSuccess(requestType, response)
                cacheAdapter?.save(repositoryClass, CacheItem(fullUrl, System.currentTimeMillis()))
                true
            } else {
                responseService?.handleStorageError(requestType, response)
                null
            }
        } else {
            responseService?.handleRequestError(requestType, response)
            null
        }
    }

    override fun onFinished() {
        super.onFinished()
        fetchSecurityLayer.onFetchFinished()
    }
}
