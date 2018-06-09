package repolizer.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import repolizer.Repolizer
import repolizer.repository.database.DatabaseBuilder
import repolizer.repository.network.NetworkBuilder
import repolizer.repository.network.FetchSecurityLayer
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseRepository<Entity> constructor(private val repolizer: Repolizer) : FetchSecurityLayer {

    private val fetchingData = AtomicBoolean(false)

    protected fun executeRefresh(builder: NetworkBuilder<Entity>): LiveData<String> {
        return builder.buildRefresh(repolizer)
                .execute(this)
    }

    protected fun executeGet(builder: NetworkBuilder<Entity>, allowFetch: Boolean): LiveData<Entity> {
        return builder.buildGet(repolizer)
                .execute(this, allowFetch)
    }

    protected fun executeCud(builder: NetworkBuilder<Entity>): LiveData<String> {
        return builder.buildCud(repolizer)
                .execute()
    }

    protected fun executeDB(builder: DatabaseBuilder<Entity>): LiveData<Boolean> {
        return builder.build()
                .execute()
    }

    override fun allowFetch(): Boolean {
        return fetchingData.compareAndSet(false, true)
    }

    override fun onFetchFinished() {
        fetchingData.set(false)
    }

    fun getContext(): Context {
        return repolizer.appContext
    }
}