package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.DatabaseOperation
import repolizer.repository.network.ExecutionType

class PersistentDatabaseFuture
constructor(repolizer: Repolizer, futureBuilder: PersistentFutureBuilder): PersistentFuture<Boolean>(repolizer, futureBuilder) {

    private val databaseOperation: DatabaseOperation = futureBuilder.databaseOperation
            ?: throw IllegalStateException("DatabaseOperation is null.")

    private val deleteSql = futureBuilder.deleteSql
    private val updateSql = futureBuilder.updateSql

    private val databaseItem: Any? = futureBuilder.databaseItem
    private val databaseItemClass: Class<*>? = futureBuilder.databaseItemClass

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, bodyType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        when(databaseOperation) {
            DatabaseOperation.INSERT -> {
                if(databaseItem == null || databaseItemClass == null) throw IllegalStateException("Database item/class is null.")
                storageAdapter.insert(repositoryClass, fullUrl, databaseItem, databaseItemClass)
            }
            DatabaseOperation.UPDATE -> {
                if(databaseItem == null || databaseItemClass == null) throw IllegalStateException("Database item/class is null.")
                storageAdapter.update(repositoryClass, fullUrl, updateSql, databaseItem, databaseItemClass)
            }
            DatabaseOperation.DELETE -> storageAdapter.delete(repositoryClass, fullUrl, deleteSql)
        }
        return true
    }
}