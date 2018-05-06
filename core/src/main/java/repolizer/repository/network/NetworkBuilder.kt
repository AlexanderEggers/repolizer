package repolizer.repository.network

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.repository.util.RequestType

class NetworkBuilder<Entity> internal constructor(val typeToken: TypeToken<*>) {

    constructor(type: Class<*>) : this(TypeToken.get(type))

    var requestType: RequestType? = null

    var url: String = ""
    var fullUrl: String = ""

    var raw: Entity? = null

    var requiresLogin: Boolean = false
    var updateDB: Boolean = false
    var showProgress: Boolean = false

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