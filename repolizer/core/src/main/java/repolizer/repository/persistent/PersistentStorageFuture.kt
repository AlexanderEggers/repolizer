package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.DataOperation
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
class PersistentStorageFuture
constructor(private val repolizer: Repolizer,
            private val futureRequest: PersistentFutureRequest) : PersistentFuture<Boolean>(repolizer, futureRequest) {

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, futureRequest.typeToken.type,
                futureRequest.repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this, futureRequest)
                ?: throw IllegalStateException("It seems like that your WrapperAdapter does not" +
                        "have the method execute() implemented.")
    }

    override fun onStart() {
        if (dataAdapter == null) throw IllegalStateException("Storage adapter is null.")
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        return when (futureRequest.dataOperation) {
            DataOperation.INSERT -> dataAdapter?.insert(futureRequest, null, futureRequest.dataObject)
            DataOperation.UPDATE -> dataAdapter?.update(futureRequest, futureRequest.dataObject)
            DataOperation.DELETE -> dataAdapter?.delete(futureRequest)
            else -> false
        }
    }
}