package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkRefreshFuture<Body>
constructor(private val repolizer: Repolizer,
            private val futureRequest: NetworkFutureRequest) : NetworkFuture<Body>(repolizer, futureRequest) {

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

    override fun onExecute(executionType: ExecutionType): Body? {
        return when (executionType) {
            ExecutionType.USE_NETWORK -> fetchFromNetwork()
            else -> null
        }
    }

    private fun fetchFromNetwork(): Body? {
        val response: NetworkResponse? = networkAdapter?.execute(futureRequest, requestProvider)

        return if (response?.isSuccessful() == true && response.body != null) {
            val convertedBody = if(response.body is String && futureRequest.bodyType != String::class.java)
                convertResponseData(response.body) else response.body as Body?

            val saveSuccessful = dataAdapter?.insert(futureRequest, convertedBody)
            if (saveSuccessful == true && cacheAdapter != null) {
                val cacheKey = cacheAdapter.getCacheKeyForNetwork(futureRequest, response)
                val successfullyCached = cacheAdapter.save(futureRequest, cacheKey)
                if (successfullyCached) {
                    repolizer.defaultMainThread.execute {
                        responseService?.handleSuccess(futureRequest)
                    }
                    convertedBody
                } else {
                    repolizer.defaultMainThread.execute {
                        responseService?.handleCacheError(futureRequest)
                    }
                    null
                }
            } else {
                repolizer.defaultMainThread.execute {
                    responseService?.handleDataError(futureRequest)
                }
                null
            }
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleRequestError(futureRequest, response)
            }
            null
        }
    }

    override fun onFinished(result: Body?) {
        super.onFinished(result)
        fetchSecurityLayer.onFetchFinished()
    }

    private fun convertResponseData(bodyData: String): Body? {
        val data: Body? = converterAdapter?.convertStringToData(
                futureRequest.repositoryClass, bodyData, futureRequest.bodyType)
        if (data == null) responseService?.handleDataError(futureRequest)
        return data
    }
}
