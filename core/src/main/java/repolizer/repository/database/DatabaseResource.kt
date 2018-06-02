package repolizer.repository.database

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import repolizer.repository.util.AppExecutor

class DatabaseResource<Entity> internal constructor(builder: DatabaseBuilder<Entity>) {

    private val result = MutableLiveData<Boolean>()

    private val databaseLayer: DatabaseLayer? = builder.databaseLayer
    private val appExecutor: AppExecutor = AppExecutor

    fun execute(): LiveData<Boolean> {
        appExecutor.workerThread.execute {
            databaseLayer?.updateDB()
            result.postValue(true)
        }
        return result
    }
}