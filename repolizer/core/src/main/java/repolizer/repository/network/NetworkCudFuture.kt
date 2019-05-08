package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.response.NetworkResponse

@Suppress("UNCHECKED_CAST")
class NetworkCudFuture<Body>
constructor(private val repolizer: Repolizer,
            private val futureRequest: NetworkFutureRequest) : NetworkFuture<Body>(repolizer, futureRequest) {

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, futureRequest.typeToken.type,
                futureRequest.repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this, futureRequest)
                ?: throw IllegalStateException("It seems like that your WrapperAdapter does not" +
                        "have the method execute() implemented.")
    }

    override fun onDetermineExecutionType(): ExecutionType {
        return ExecutionType.USE_NETWORK
    }

    override fun onExecute(executionType: ExecutionType): Body? {
        val response: NetworkResponse? = networkAdapter?.execute(futureRequest, requestProvider)

        return if (response?.isSuccessful() == true && response.body?.javaClass == futureRequest.bodyType.javaClass) {
            repolizer.defaultMainThread.execute {
                responseService?.handleSuccess(futureRequest)
            }
            response.body as Body?
        } else {
            repolizer.defaultMainThread.execute {
                responseService?.handleRequestError(futureRequest, response)
            }
            null
        }
    }
}
