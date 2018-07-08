package repolizer.repository.network

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.repository.persistent.PersistentFutureBuilder
import repolizer.repository.progress.ProgressData
import repolizer.repository.request.RequestType

open class NetworkFutureBuilder<Entity>: PersistentFutureBuilder() {

    var requestType: RequestType? = null

    var url: String = ""

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

    var requiresLogin: Boolean = false
    var showProgress: Boolean = false
    var isDeletingCacheIfTooOld: Boolean = false

    var networkLayer: NetworkLayer<Entity>? = null

    val headerMap: HashMap<String, String> = HashMap()
    val queryMap: HashMap<String, String> = HashMap()

    fun addHeader(key: String, value: String) {
        headerMap[key] = value
    }

    fun addQuery(key: String, value: String) {
        queryMap[key] = value
    }

    fun buildGet(repolizer: Repolizer): NetworkGetFuture<Entity> {
        return NetworkGetFuture(repolizer, this)
    }

    fun buildRefresh(repolizer: Repolizer): NetworkRefreshFuture<Entity> {
        return NetworkRefreshFuture(repolizer, this)
    }

    fun buildCud(repolizer: Repolizer): NetworkCudFuture<Entity> {
        return NetworkCudFuture(repolizer, this)
    }
}