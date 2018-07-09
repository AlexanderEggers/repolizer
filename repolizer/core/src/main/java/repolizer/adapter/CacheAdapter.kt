package repolizer.adapter

import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState

interface CacheAdapter {

    fun save(repositoryClass: Class<*>, data: CacheItem)

    fun get(repositoryClass: Class<*>, url: String, freshTime: Long, maxTime: Long): CacheState

    fun delete(repositoryClass: Class<*>, url: String)
}
