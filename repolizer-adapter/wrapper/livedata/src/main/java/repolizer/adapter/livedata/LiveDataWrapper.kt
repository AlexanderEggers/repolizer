package repolizer.adapter.livedata

import android.arch.lifecycle.LiveData
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AppExecutor
import repolizer.repository.future.Future
import java.util.concurrent.atomic.AtomicBoolean

class LiveDataWrapper: WrapperAdapter<LiveData<*>>() {

    private val appExecutor = AppExecutor

    override fun <B> execute(future: Future<B>): LiveData<B> {
        return object : LiveData<B>() {
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

    override fun canHaveStorageConnection(): Boolean {
        return true
    }
}