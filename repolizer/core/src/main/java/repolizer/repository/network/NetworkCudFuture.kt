package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkCudFuture
constructor(repolizer: Repolizer,
            futureRequest: NetworkFutureRequest) : NetworkFuture<String>(repolizer, futureRequest) {

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, wrapperType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onDetermineExecutionType(): ExecutionType {
        return ExecutionType.USE_NETWORK
    }

    override fun onExecute(executionType: ExecutionType): String? {
        val response: NetworkResponse<String>? = networkAdapter?.execute(this, requestProvider)

        return if (response?.isSuccessful() == true) {
            repolizer.defaultMainThread.execute {
                responseService?.handleSuccess(requestType, futureRequest)
            }
            response.body
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleRequestError(requestType, futureRequest, response)
            }
            null
        }
    }
}
