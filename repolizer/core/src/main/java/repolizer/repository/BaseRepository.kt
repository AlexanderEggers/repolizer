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
        val lowestBodyClass = getLowestBodyClass(returnType)
        val hasList = hasListType(returnType)

        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        val future = if (hasList) futureRequest.buildRefreshWithList(repolizer, lowestBodyClass)
        else futureRequest.buildRefresh(repolizer, lowestBodyClass)
        return future.create()
    }

    protected fun <T> executeGet(futureRequest: NetworkFutureRequestBuilder, returnType: Type): T {
        val lowestBodyClass = getLowestBodyClass(returnType)
        val hasList = hasListType(returnType)

        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        val future = if (hasList) futureRequest.buildGetWithList(repolizer, lowestBodyClass)
        else futureRequest.buildGet(repolizer, lowestBodyClass)
        return future.create()
    }

    protected fun <T> executeCud(futureRequest: NetworkFutureRequestBuilder, returnType: Type): T {
        val lowestBodyClass = getLowestBodyClass(returnType)
        val hasList = hasListType(returnType)

        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        val future = if (hasList) futureRequest.buildCudWithList(repolizer, lowestBodyClass)
        else futureRequest.buildCud(repolizer, lowestBodyClass)
        return future.create()
    }

    protected fun <T> executeData(futureRequest: PersistentFutureRequestBuilder, returnType: Type): T {
        val lowestBodyClass = getLowestBodyClass(returnType)
        val hasList = hasListType(returnType)

        val bodyType = getBodyType(returnType)
        futureRequest.bodyType = bodyType

        val future = if (hasList) futureRequest.buildDataWithList(repolizer, lowestBodyClass)
        else futureRequest.buildData(repolizer, lowestBodyClass)
        return future.create()
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