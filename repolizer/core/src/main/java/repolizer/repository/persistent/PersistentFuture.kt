package repolizer.repository.persistent

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import repolizer.repository.util.AppExecutor

class PersistentFuture internal constructor(builder: PersistentFutureBuilder) {

    private val result = MutableLiveData<Boolean>()

    private val appExecutor: AppExecutor = AppExecutor
    private val persistentLayer: PersistentLayer = builder.persistentLayer
            ?: throw IllegalStateException("Internal error: Layer is null.")

    fun execute(): LiveData<Boolean> {
        appExecutor.workerThread.execute {
            persistentLayer.updateDB()
            result.postValue(true)
        }
        return result
    }
}