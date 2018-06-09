package repolizer.repository.network

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.repository.progress.ProgressData
import repolizer.repository.response.RequestType

class NetworkBuilder<Entity> {

    var requestType: RequestType? = null

    var url: String = ""

    var raw: Entity? = null
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

    fun buildGet(repolizer: Repolizer): NetworkGetResource<Entity> {
        return NetworkGetResource(repolizer, this)
    }

    fun buildRefresh(repolizer: Repolizer): NetworkRefreshResource<Entity> {
        return NetworkRefreshResource(repolizer, this)
    }

    fun buildCud(repolizer: Repolizer): NetworkCudResource<Entity> {
        return NetworkCudResource(repolizer, this)
    }
}