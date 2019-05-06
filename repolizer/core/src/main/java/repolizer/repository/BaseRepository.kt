package repolizer.repository

import repolizer.Repolizer
import repolizer.adapter.util.AdapterUtil.Companion.getBodyType
import repolizer.adapter.util.AdapterUtil.Companion.getLowestBodyClass
import repolizer.adapter.util.AdapterUtil.Companion.hasListType
import repolizer.repository.network.FetchSecurityLayer
import repolizer.repository.network.NetworkFutureRequestBuilder
import repolizer.repository.persistent.PersistentFutureRequestBuilder
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseRepository constructor(private val repolizer: Repolizer) : FetchSecurityLayer {

    private val fetchingData = AtomicBoolean(false)

    protected fun <T> executeRefresh(futureRequest: NetworkFutureRequestBuilder, returnType: Type): T {
        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        return futureRequest.buildRefresh(repolizer).create()
    }

    protected fun <T> executeGet(futureRequest: NetworkFutureRequestBuilder, returnType: Type): T {
        val lowestBodyClass = getLowestBodyClass(returnType)
        val hasList = hasListType(returnType)

        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        return if (hasList) {
            futureRequest.buildGet(repolizer, lowestBodyClass).create()
        } else {
            futureRequest.buildGetWithList(repolizer, lowestBodyClass).create()
        }
    }

    protected fun <T> executeCud(futureRequest: NetworkFutureRequestBuilder, returnType: Type): T {
        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        return futureRequest.buildCud(repolizer).create()
    }

    protected fun <T> executeStorage(futureRequest: PersistentFutureRequestBuilder, returnType: Type): T {
        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        return futureRequest.buildStorage(repolizer).create()
    }

    protected fun <T> executeCache(futureRequest: PersistentFutureRequestBuilder, returnType: Type): T {
        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        return futureRequest.buildCache(repolizer).create()
    }

    override fun allowFetch(): Boolean {
        return fetchingData.compareAndSet(false, true)
    }

    override fun onFetchFinished() {
        fetchingData.set(false)
    }
}