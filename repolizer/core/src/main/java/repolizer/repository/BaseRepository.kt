package repolizer.repository

import repolizer.Repolizer
import repolizer.adapter.util.AdapterUtil.Companion.getBodyType
import repolizer.adapter.util.AdapterUtil.Companion.getLowestBodyClass
import repolizer.adapter.util.AdapterUtil.Companion.hasListType
import repolizer.repository.network.FetchSecurityLayer
import repolizer.repository.network.NetworkFutureBuilder
import repolizer.repository.persistent.PersistentFutureBuilder
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseRepository constructor(private val repolizer: Repolizer) : FetchSecurityLayer {

    private val fetchingData = AtomicBoolean(false)

    protected fun <T> executeRefresh(futureBuilder: NetworkFutureBuilder, returnType: Type): T {
        val bodyType = getBodyType(returnType)
        futureBuilder.bodyType = bodyType

        return futureBuilder.buildRefresh(repolizer).create()
    }

    protected fun <T> executeGet(futureBuilder: NetworkFutureBuilder, returnType: Type): T {
        val lowestBodyClass = getLowestBodyClass(returnType)
        val hasList = hasListType(returnType)

        val bodyType = getBodyType(returnType)
        futureBuilder.bodyType = bodyType

        return if (hasList) {
            futureBuilder.buildGet(repolizer, lowestBodyClass).create()
        } else {
            futureBuilder.buildGetWithList(repolizer, lowestBodyClass).create()
        }
    }

    protected fun <T> executeCud(futureBuilder: NetworkFutureBuilder, returnType: Type): T {
        val bodyType = getBodyType(returnType)
        futureBuilder.bodyType = bodyType

        return futureBuilder.buildCud(repolizer).create()
    }

    protected fun <T> executeStorage(builder: PersistentFutureBuilder, returnType: Type): T {
        val bodyType = getBodyType(returnType)
        builder.bodyType = bodyType

        return builder.buildStorage(repolizer).create()
    }

    protected fun <T> executeCache(builder: PersistentFutureBuilder, returnType: Type): T {
        val bodyType = getBodyType(returnType)
        builder.bodyType = bodyType

        return builder.buildCache(repolizer).create()
    }

    override fun allowFetch(): Boolean {
        return fetchingData.compareAndSet(false, true)
    }

    override fun onFetchFinished() {
        fetchingData.set(false)
    }
}