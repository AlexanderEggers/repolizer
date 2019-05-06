package repolizer.repository.network

import repolizer.Repolizer
import repolizer.repository.future.FutureRequest
import repolizer.repository.request.RequestType
import repolizer.repository.util.QueryHashMap

open class NetworkFutureRequest(repolizer: Repolizer, builder: NetworkFutureRequestBuilder) : FutureRequest(builder) {

    val fetchSecurityLayer: FetchSecurityLayer = builder.fetchSecurityLayer
    val requestType: RequestType = builder.requestType
            ?: throw IllegalStateException("Request type is null.")

    val fullUrl: String by lazy {
        repolizer.baseUrl?.let { baseUrl ->
            "$baseUrl${builder.url}"
        } ?: builder.url
    }

    val rawObjects = builder.rawObjects
    val partObjects = builder.partObjects

    val freshCacheTime: Long = builder.freshCacheTime
    val maxCacheTime: Long = builder.maxCacheTime

    val requiresLogin: Boolean = builder.requiresLogin
    val saveData: Boolean = builder.saveData
    val connectionOnly: Boolean = builder.connectionOnly

    val allowFetch: Boolean = builder.allowFetch
    val isDeletingCacheIfTooOld: Boolean = builder.isDeletingCacheIfTooOld
    val allowMultipleRequestsAtSameTime: Boolean = builder.allowMultipleRequestsAtSameTime

    val headerMap: HashMap<String, String> = builder.headerMap
    val queryMap: QueryHashMap = builder.queryMap
}