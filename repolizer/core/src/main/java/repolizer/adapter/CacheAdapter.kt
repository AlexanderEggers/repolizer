package repolizer.adapter

import repolizer.persistent.CacheState
import repolizer.repository.future.FutureRequest
import repolizer.repository.network.NetworkFutureRequest
import repolizer.repository.response.NetworkResponse

abstract class CacheAdapter {

    abstract fun save(request: FutureRequest, cacheObject: Any?): Boolean

    abstract fun get(request: FutureRequest, key: String, freshCacheTime: Long, maxCacheTime: Long): CacheState

    abstract fun delete(request: FutureRequest, cacheObject: Any?): Boolean

    open fun getCacheKeyForNetwork(request: NetworkFutureRequest, response: NetworkResponse?): String {
        return if(request.cacheKey.isNotBlank()) request.cacheKey else request.fullUrl
    }
}
