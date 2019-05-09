package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.DataOperation
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
class PersistentDataFuture<Body>
constructor(private val repolizer: Repolizer,
            private val futureRequest: PersistentFutureRequest) : PersistentFuture<Body>(repolizer, futureRequest) {

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, futureRequest.typeToken.type,
                futureRequest.repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this, futureRequest)
                ?: throw IllegalStateException("It seems like that your WrapperAdapter does not" +
                        "have the method execute() implemented.")
    }

    override fun onStart() {
        if (dataAdapter == null) throw IllegalStateException("Data adapter is null.")
    }

    override fun onExecute(executionType: ExecutionType): Body? {
        return when (futureRequest.dataOperation) {
            DataOperation.INSERT -> {
                val successful = dataAdapter?.insert(futureRequest, futureRequest.dataObject as Body?)
                handleDataOperation(successful)
            }
            DataOperation.UPDATE -> {
                val successful = dataAdapter?.update(futureRequest, futureRequest.dataObject as Body?)
                handleDataOperation(successful)
            }
            DataOperation.DELETE -> {
                val successful = dataAdapter?.delete(futureRequest)
                handleDataOperation(successful)
            }
            else -> null
        }
    }

    private fun handleDataOperation(successful: Boolean?): Body? {
        return if (successful == true && (futureRequest.returnStatement?.isNotBlank() == true
                        || futureRequest.overrideEmptyReturnStatement)) {
            dataAdapter?.get(futureRequest)
        } else {
            if(successful != true) {
                repolizer.defaultMainThread.execute { responseService?.handleDataError(futureRequest) }
            }
            null
        }
    }
}