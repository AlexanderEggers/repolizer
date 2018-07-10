package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.persistent.CacheItem
import repolizer.repository.response.NetworkResponse

class NetworkRefreshFuture
constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder) : NetworkFuture<Boolean>(repolizer, futureBuilder) {

    private var fetchSecurityLayer: FetchSecurityLayer = futureBuilder.fetchSecurityLayer
            ?: throw IllegalStateException("FetchSecurityLayer is null.")

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, bodyType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onDetermineExecutionType(): ExecutionType {
        return ExecutionType.USE_NETWORK
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        val response: NetworkResponse<String> = networkAdapter.execute(this, requestProvider)

        return if (response.isSuccessful() && response.body != null) {
            val saveSuccessful = storageAdapter.insert(repositoryClass, fullUrl, response.body, String::class.java)
            if(saveSuccessful) {
                responseService?.handleSuccess(requestType, response)
                cacheAdapter.save(repositoryClass, CacheItem(fullUrl, System.currentTimeMillis()))
                true
            } else {
                responseService?.handleDatabaseError(requestType, response)
                false
            }
        } else {
            responseService?.handleRequestError(requestType, response)
            false
        }
    }

    override fun onFinished() {
        super.onFinished()
        fetchSecurityLayer.onFetchFinished()
    }
}
