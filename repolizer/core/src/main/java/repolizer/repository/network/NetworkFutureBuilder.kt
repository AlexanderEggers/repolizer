package repolizer.repository.network

import repolizer.Repolizer
import repolizer.repository.future.FutureBuilder
import repolizer.repository.progress.ProgressData
import repolizer.repository.request.RequestType
import repolizer.repository.util.QueryHashMap
import java.lang.reflect.Type

open class NetworkFutureBuilder : FutureBuilder() {

    var requestType: RequestType? = null

    var raw: Any? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE raw body can be set. Make sure that you don't " +
                        "use more than one @RequestBody parameter for this method.")
            } else field = value
        }

    var progressData: ProgressData? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE Progress object can be set. Make sure " +
                        "that you don't use more than one @Progress parameter for this method.")
            } else field = value
        }

    var freshCacheTime: Long = Long.MAX_VALUE
    var maxCacheTime: Long = Long.MAX_VALUE

    var fetchSecurityLayer: FetchSecurityLayer? = null
    var requiresLogin: Boolean = false
    var showProgress: Boolean = false
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

    open fun <Body> buildGet(repolizer: Repolizer, returnType: Body): NetworkGetFuture<Body> {
        return NetworkGetFuture(repolizer, this)
    }

    open fun buildRefresh(repolizer: Repolizer): NetworkRefreshFuture {
        return NetworkRefreshFuture(repolizer, this)
    }

    open fun buildCud(repolizer: Repolizer): NetworkCudFuture {
        return NetworkCudFuture(repolizer, this)
    }
}