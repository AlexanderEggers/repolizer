package repolizer.persistent

class CacheItem(val url: String, var cacheTime: Long = System.currentTimeMillis())