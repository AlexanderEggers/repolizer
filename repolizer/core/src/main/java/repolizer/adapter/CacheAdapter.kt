package repolizer.adapter

import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState
import repolizer.repository.future.FutureRequest

abstract class CacheAdapter {

    abstract fun save(request: FutureRequest, data: CacheItem): Boolean

    abstract fun get(request: FutureRequest, key: String, freshCacheTime: Long, maxCacheTime: Long): CacheState

    abstract fun delete(request: FutureRequest, data: CacheItem): Boolean
}
