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

    private val deleteSql = futureBuilder.deleteSql
    private val updateSql = futureBuilder.updateSql

    private val databaseItem: Any? = futureBuilder.storageItem
    private val databaseItemClass: Class<*>? = futureBuilder.storageItemClass

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, bodyType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        when(storageOperation) {
            StorageOperation.INSERT -> {
                if(databaseItem == null || databaseItemClass == null) throw IllegalStateException("Database item/class is null.")
                storageAdapter.insert(repositoryClass, fullUrl, databaseItem, databaseItemClass)
            }
            StorageOperation.UPDATE -> {
                if(databaseItem == null || databaseItemClass == null) throw IllegalStateException("Database item/class is null.")
                storageAdapter.update(repositoryClass, fullUrl, updateSql, databaseItem, databaseItemClass)
            }
            StorageOperation.DELETE -> storageAdapter.delete(repositoryClass, fullUrl, deleteSql)
        }
        return true
    }
}