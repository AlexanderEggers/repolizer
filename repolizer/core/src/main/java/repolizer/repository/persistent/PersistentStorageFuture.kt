package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.StorageOperation
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
class PersistentStorageFuture
constructor(private val repolizer: Repolizer,
            private  val futureRequest: PersistentFutureRequest) : PersistentFuture<Boolean>(repolizer, futureRequest) {

    private val databaseItem: Any? = futureRequest.storageItem

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, futureRequest.typeToken.type,
                futureRequest.repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this, futureRequest)
                ?: throw IllegalStateException("It seems like that your WrapperAdapter does not" +
                        "have the method execute() implemented.")
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        if (storageAdapter == null) throw IllegalStateException("Storage adapter is null.")
        else return when (futureRequest.storageOperation) {
            StorageOperation.INSERT -> {
                if (databaseItem == null) throw IllegalStateException("Database item is null.")
                storageAdapter.insert(futureRequest, null, databaseItem)
            }
            StorageOperation.UPDATE -> {
                storageAdapter.update(futureRequest, databaseItem)
            }
            StorageOperation.DELETE -> storageAdapter.delete(futureRequest)
        }
    }
}