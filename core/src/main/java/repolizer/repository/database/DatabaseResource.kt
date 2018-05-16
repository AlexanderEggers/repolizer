package repolizer.repository.database

import repolizer.repository.util.AppExecutor

class DatabaseResource<Entity> internal constructor(builder: DatabaseBuilder<Entity>) {

    private val databaseLayer: DatabaseLayer? = builder.databaseLayer
    private val appExecutor: AppExecutor = AppExecutor

    fun execute() {
        appExecutor.workerThread.execute {
            databaseLayer?.updateDB()
        }
    }
}