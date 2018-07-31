package repolizer.adapter.wrapper.livedata

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import repolizer.adapter.StorageAdapter
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.wrapper.util.AppExecutor
import repolizer.repository.future.Future
import java.util.concurrent.atomic.AtomicBoolean

class LiveDataWrapper: WrapperAdapter<LiveData<*>>() {

    private val appExecutor = AppExecutor

    override fun <B> execute(future: Future<B>): LiveData<B> {
        return object : MediatorLiveData<B>() {
            var started = AtomicBoolean(false)

            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    appExecutor.workerThread.execute {
                        postValue(future.execute())
                    }
                }
            }
        }
    }

    override fun <B> execute(future: Future<B>, storageAdapter: StorageAdapter<B>,
                             repositoryClass: Class<*>, url: String, sql: String): LiveData<B>? {
        appExecutor.workerThread.execute {
            future.execute()
        }

        return storageAdapter.establishConnection(repositoryClass, url, sql)
    }

    override fun canHaveStorageConnection(): Boolean {
        return true
    }
}