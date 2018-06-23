package repolizer.repository.database

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import repolizer.repository.util.AppExecutor

class DatabaseResource internal constructor(builder: DatabaseBuilder) {

    private val result = MutableLiveData<Boolean>()

    private val appExecutor: AppExecutor = AppExecutor
    private val databaseLayer: DatabaseLayer = builder.databaseLayer
            ?: throw IllegalStateException("Internal error: Database layer is null.")

    fun execute(): LiveData<Boolean> {
        appExecutor.workerThread.execute {
            databaseLayer.updateDB()
            result.postValue(true)
        }
        return result
    }
}