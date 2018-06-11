package repolizer.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import repolizer.Repolizer
import repolizer.repository.database.DatabaseBuilder
import repolizer.repository.network.NetworkBuilder
import repolizer.repository.network.FetchSecurityLayer
import java.io.Serializable
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseRepository constructor(private val repolizer: Repolizer) : FetchSecurityLayer {

    private val fetchingData = AtomicBoolean(false)

    protected fun executeRefresh(builder: NetworkBuilder<*>): LiveData<String> {
        return builder.buildRefresh(repolizer)
                .execute(this)
    }

    protected fun <T> executeGet(builder: NetworkBuilder<T>, allowFetch: Boolean): LiveData<T> {
        return builder.buildGet(repolizer)
                .execute(this, allowFetch)
    }

    protected fun executeCud(builder: NetworkBuilder<Serializable>): LiveData<String> {
        return builder.buildCud(repolizer)
                .execute()
    }

    protected fun executeDB(builder: DatabaseBuilder<*>): LiveData<Boolean> {
        return builder.build()
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