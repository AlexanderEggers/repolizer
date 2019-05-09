package repolizer.adapter

import repolizer.repository.future.FutureRequest
import repolizer.repository.network.NetworkFutureRequest
import repolizer.repository.response.NetworkResponse
import repolizer.repository.util.CacheState

abstract class CacheAdapter {

    abstract fun save(request: FutureRequest, key: String?): Boolean

    abstract fun get(request: FutureRequest, key: String, freshCacheTime: Long, maxCacheTime: Long): CacheState

    abstract fun delete(request: FutureRequest, key: String?): Boolean

    open fun getCacheKeyForNetwork(request: NetworkFutureRequest, response: NetworkResponse?): String {
        return if (request.cacheKey.isNotBlank()) request.cacheKey else request.fullUrl
    }
}
