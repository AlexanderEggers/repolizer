package repolizer.repository.network

import repolizer.Repolizer
import repolizer.repository.future.FutureBuilder
import repolizer.repository.request.RequestType
import repolizer.repository.util.QueryHashMap

open class NetworkFutureBuilder : FutureBuilder() {

    var requestType: RequestType? = null

    val rawObjects = ArrayList<Any?>()
    val partObjects = ArrayList<Any?>()

    var freshCacheTime: Long = Long.MAX_VALUE
    var maxCacheTime: Long = Long.MAX_VALUE

    var fetchSecurityLayer: FetchSecurityLayer? = null
    var requiresLogin: Boolean = false
    var saveData: Boolean = true

    var allowFetch: Boolean = false
    var isDeletingCacheIfTooOld: Boolean = false
    var allowMultipleRequestsAtSameTime: Boolean = false

    val headerMap: HashMap<String, String> = HashMap()
    val queryMap: QueryHashMap = QueryHashMap()

    open fun addHeader(key: String, value: String) {
        headerMap[key] = value
    }

    @Suppress("unchecked_cast")
    open fun addQuery(key: String, value: String) {
        val list = queryMap[key] as? ArrayList<String> ?: ArrayList()
        list.add(value)
        queryMap[key] = list
    }

    open fun addRaw(raw: Any?) {
        rawObjects.add(raw)
    }

    open fun addMultipartBody(body: Any?) {
        partObjects.add(body)
    }

    open fun <Body> buildGet(repolizer: Repolizer, returnType: Class<Body>): NetworkGetFuture<Body> {
        return NetworkGetFuture(repolizer, this)
    }

    open fun <Body> buildGetWithList(repolizer: Repolizer, returnType: Class<Body>): NetworkGetFuture<List<Body>> {
        return NetworkGetFuture(repolizer, this)
    }

    open fun buildRefresh(repolizer: Repolizer): NetworkRefreshFuture {
        return NetworkRefreshFuture(repolizer, this)
    }

    open fun buildCud(repolizer: Repolizer): NetworkCudFuture {
        return NetworkCudFuture(repolizer, this)
    }
}