package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.StorageOperation
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
class PersistentStorageFuture
constructor(repolizer: Repolizer, futureRequest: PersistentFutureRequest) : PersistentFuture<Boolean>(repolizer, futureRequest) {

    private val storageOperation: StorageOperation = futureRequest.storageOperation
            ?: throw IllegalStateException("StorageOperation is null.")

    private val insertSql = futureRequest.insertSql
    private val updateSql = futureRequest.updateSql
    private val deleteSql = futureRequest.deleteSql

    private val databaseItem: Any? = futureRequest.storageItem

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, wrapperType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        if (storageAdapter == null) throw IllegalStateException("Storage adapter is null.")
        else return when (storageOperation) {
            StorageOperation.INSERT -> {
                if (databaseItem == null) throw IllegalStateException("Database item is null.")
                storageAdapter.insert(repositoryClass, null, "", insertSql, databaseItem, bodyType)
            }
            StorageOperation.UPDATE -> {
                storageAdapter.update(repositoryClass, updateSql, databaseItem)
            }
            StorageOperation.DELETE -> storageAdapter.delete(repositoryClass, "", deleteSql)
        }
    }
}