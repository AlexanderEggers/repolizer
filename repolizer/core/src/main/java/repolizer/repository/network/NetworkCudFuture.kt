package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkCudFuture
constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder) : NetworkFuture<String>(repolizer, futureBuilder) {

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, wrapperType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onDetermineExecutionType(): ExecutionType {
        return ExecutionType.USE_NETWORK
    }

    override fun onExecute(executionType: ExecutionType): String? {
        val response: NetworkResponse<String> = networkAdapter?.execute(this, requestProvider)
                ?: throw IllegalStateException("Network Adapter error: Your url that you have " +
                        "set inside your repository method is empty.")

        return if (response.isSuccessful()) {
            responseService?.handleSuccess(requestType, response)
            response.body
        } else {
            responseService?.handleRequestError(requestType, response)
            null
        }
    }
}
