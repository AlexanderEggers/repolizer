package repolizer.repository.network

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.repository.future.FutureBuilder
import repolizer.repository.progress.ProgressData
import repolizer.repository.request.RequestType

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

    var typeToken: TypeToken<*>? = null

    var freshCacheTime: Long = Long.MAX_VALUE
    var maxCacheTime: Long = Long.MAX_VALUE

    var fetchSecurityLayer: FetchSecurityLayer? = null
    var allowFetch: Boolean = false
    var requiresLogin: Boolean = false
    var showProgress: Boolean = false
    var isDeletingCacheIfTooOld: Boolean = false

    val headerMap: HashMap<String, String> = HashMap()
    val queryMap: HashMap<String, String> = HashMap()

    open fun addHeader(key: String, value: String) {
        headerMap[key] = value
    }

    open fun addQuery(key: String, value: String) {
        queryMap[key] = value
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