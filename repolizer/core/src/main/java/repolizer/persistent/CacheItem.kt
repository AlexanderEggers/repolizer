package repolizer.persistent

class CacheItem @JvmOverloads constructor(val key: String, var cacheTime: Long = System.currentTimeMillis())