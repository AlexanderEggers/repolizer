package repolizer.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import repolizer.Repolizer
import repolizer.repository.persistent.PersistentFutureBuilder
import repolizer.repository.network.NetworkFutureBuilder
import repolizer.repository.network.FetchSecurityLayer
import java.io.Serializable
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseRepository constructor(private val repolizer: Repolizer) : FetchSecurityLayer {

    private val fetchingData = AtomicBoolean(false)

    protected fun executeRefresh(futureBuilder: NetworkFutureBuilder<*>): LiveData<Boolean> {
        return futureBuilder.buildRefresh(repolizer)
                .execute(this)
    }

    protected fun <T> executeGet(futureBuilder: NetworkFutureBuilder<T>, allowFetch: Boolean): T {
        return futureBuilder.buildGet(repolizer)
                .execute(this, allowFetch)
    }

    protected fun executeCud(futureBuilder: NetworkFutureBuilder<Serializable>): LiveData<String> {
        return futureBuilder.buildCud(repolizer)
                .execute()
    }

    protected fun executeDB(builder: PersistentFutureBuilder): LiveData<Boolean> {
        return builder.buildCache()
                .execute()
    }

    override fun allowFetch(): Boolean {
        return fetchingData.compareAndSet(false, true)
    }

    override fun onFetchFinished() {
        fetchingData.set(false)
    }

    fun getAppContext(): Context {
        return repolizer.appContext
    }
}