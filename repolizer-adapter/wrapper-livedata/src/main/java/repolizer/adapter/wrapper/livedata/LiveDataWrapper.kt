package repolizer.adapter.wrapper.livedata

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import repolizer.adapter.StorageAdapter
import repolizer.adapter.WrapperAdapter
import repolizer.repository.future.Future
import repolizer.repository.future.FutureCallback
import java.util.concurrent.atomic.AtomicBoolean

class LiveDataWrapper: WrapperAdapter<LiveData<*>>() {

    override fun <B> execute(future: Future<B>): LiveData<B> {
        return object : MediatorLiveData<B>() {
            var started = AtomicBoolean(false)

            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    future.executeAsync(object: FutureCallback<B> {

                        override fun onFinished(body: B?) {
                            postValue(body)
                        }
                    })
                }
            }
        }
    }

    override fun <B> execute(future: Future<B>, storageAdapter: StorageAdapter<B>,
                             repositoryClass: Class<*>, url: String, sql: String): LiveData<B>? {
        future.executeAsync()
        return storageAdapter.establishConnection(repositoryClass, url, sql)
    }

    override fun canHaveStorageConnection(): Boolean {
        return true
    }
}