package repolizer.adapter

import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState

abstract class CacheAdapter {

    abstract fun save(repositoryClass: Class<*>, data: CacheItem)

    abstract fun get(repositoryClass: Class<*>, url: String, freshCacheTime: Long, maxCacheTime: Long): CacheState

    abstract fun delete(repositoryClass: Class<*>, data: CacheItem)
}
