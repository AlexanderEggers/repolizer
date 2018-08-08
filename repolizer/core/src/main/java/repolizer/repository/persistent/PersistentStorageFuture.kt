package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.StorageOperation
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
class PersistentStorageFuture
constructor(repolizer: Repolizer, futureBuilder: PersistentFutureBuilder) : PersistentFuture<Boolean>(repolizer, futureBuilder) {

    private val storageOperation: StorageOperation = futureBuilder.storageOperation
            ?: throw IllegalStateException("StorageOperation is null.")

    private val insertSql = futureBuilder.insertSql
    private val updateSql = futureBuilder.updateSql
    private val deleteSql = futureBuilder.deleteSql

    private val databaseItem: Any? = futureBuilder.storageItem

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, wrapperType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        return when (storageOperation) {
            StorageOperation.INSERT -> {
                if (databaseItem == null) throw IllegalStateException("Database item is null.")
                storageAdapter.insert(repositoryClass, null,"", insertSql, databaseItem, bodyType)
            }
            StorageOperation.UPDATE -> {
                storageAdapter.update(repositoryClass, updateSql, databaseItem)
            }
            StorageOperation.DELETE -> storageAdapter.delete(repositoryClass, "", deleteSql)
        }
    }
}