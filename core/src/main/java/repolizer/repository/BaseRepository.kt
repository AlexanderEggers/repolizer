package repolizer.repository

import android.arch.lifecycle.LiveData
import android.content.Context
import repolizer.Repolizer
import repolizer.repository.database.DatabaseBuilder
import repolizer.repository.network.NetworkBuilder
import repolizer.repository.util.FetchSecurityLayer
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseRepository<Entity> constructor(private val repolizer: Repolizer) : FetchSecurityLayer {

    private val refreshing = AtomicBoolean(false)

    protected fun executeRefresh(builder: NetworkBuilder<Entity>): LiveData<String> {
        return builder.buildRefresh(repolizer)
                .execute(this)
    }

    protected fun executeGet(builder: NetworkBuilder<Entity>, allowFetch: Boolean): LiveData<Entity> {
        return builder.buildGet(repolizer)
                .execute(this, allowFetch)
    }

    protected fun executePost(builder: NetworkBuilder<Entity>): LiveData<String> {
        return builder.buildCud(repolizer)
                .execute()
    }

    protected fun executePut(builder: NetworkBuilder<Entity>): LiveData<String> {
        return builder.buildCud(repolizer)
                .execute()
    }

    protected fun executeDelete(builder: NetworkBuilder<Entity>): LiveData<String> {
        return builder.buildCud(repolizer)
                .execute()
    }

    protected fun executeDB(builder: DatabaseBuilder<Entity>) {
        builder.build()
                .execute()
    }

    override fun allowFetch(): Boolean {
        return refreshing.compareAndSet(false, true)
    }

    override fun onFetchFinished() {
        refreshing.set(false)
    }

    fun getContext(): Context {
        return repolizer.context
    }
}