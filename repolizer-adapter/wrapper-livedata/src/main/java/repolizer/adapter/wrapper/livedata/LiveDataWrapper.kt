package repolizer.adapter.wrapper.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import repolizer.adapter.DataAdapter
import repolizer.adapter.WrapperAdapter
import repolizer.repository.future.Future
import repolizer.repository.future.FutureCallback
import repolizer.repository.future.FutureRequest
import repolizer.repository.network.NetworkFutureRequest
import java.util.concurrent.atomic.AtomicBoolean

class LiveDataWrapper : WrapperAdapter<LiveData<*>>() {

    override fun <B> execute(future: Future<B>, request: FutureRequest): LiveData<B> {
        return object : MediatorLiveData<B>() {
            var started = AtomicBoolean(false)

            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    future.executeAsync(object : FutureCallback<B> {

                        override fun onFinished(body: B?) {
                            postValue(body)
                        }
                    })
                }
            }
        }
    }

    override fun <B> establishDataConnection(future: Future<B>, request: NetworkFutureRequest, dataAdapter: DataAdapter<B>): LiveData<B>? {
        if (!request.connectionOnly) future.executeAsync()
        return dataAdapter.establishConnection(request)
    }

    override fun canHaveDataConnection(): Boolean {
        return true
    }
}