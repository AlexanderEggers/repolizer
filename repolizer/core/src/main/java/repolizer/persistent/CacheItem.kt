package repolizer.persistent

class CacheItem @JvmOverloads constructor(val url: String, var cacheTime: Long = System.currentTimeMillis())