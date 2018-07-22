package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.StorageOperation
import repolizer.repository.network.ExecutionType

class PersistentStorageFuture
constructor(repolizer: Repolizer, futureBuilder: PersistentFutureBuilder): PersistentFuture<Boolean>(repolizer, futureBuilder) {

    private val storageOperation: StorageOperation = futureBuilder.storageOperation
            ?: throw IllegalStateException("StorageOperation is null.")

    private val insertSql = futureBuilder.insertSql
    private val updateSql = futureBuilder.updateSql
    private val deleteSql = futureBuilder.deleteSql

    private val databaseItem: Any? = futureBuilder.storageItem

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, bodyType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        when(storageOperation) {
            StorageOperation.INSERT -> {
                if(databaseItem == null) throw IllegalStateException("Database item is null.")
                storageAdapter.insert(repositoryClass, fullUrl, insertSql, databaseItem, String::class.java)
            }
            StorageOperation.UPDATE -> {
                if(databaseItem == null) throw IllegalStateException("Database item is null.")
                storageAdapter.update(repositoryClass, fullUrl, updateSql, databaseItem, String::class.java)
            }
            StorageOperation.DELETE -> storageAdapter.delete(repositoryClass, fullUrl, deleteSql)
        }
        return true
    }
}