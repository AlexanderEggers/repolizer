package repolizer.adapter.cache.sharedprefs

import android.content.Context
import repolizer.adapter.CacheAdapter
import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState
import repolizer.repository.future.FutureRequest

class SharedPrefCacheAdapter(private val context: Context) : CacheAdapter() {

    override fun save(request: FutureRequest, data: CacheItem): Boolean {
        val prefs = context.getSharedPreferences(DEFAULT_CACHE_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putLong(data.url, data.cacheTime)
        edit.apply()
        return true
    }

    override fun get(request: FutureRequest, key: String, freshCacheTime: Long, maxCacheTime: Long): CacheState {
        val prefs = context.getSharedPreferences(DEFAULT_CACHE_SHARED_PREFS_NAME, Context.MODE_PRIVATE)

        val lastUpdated = prefs.getLong(key, 0L)
        if (lastUpdated == 0L) return CacheState.NO_CACHE

        val diff = System.currentTimeMillis() - lastUpdated

        return when {
            diff >= maxCacheTime -> CacheState.NEEDS_HARD_REFRESH
            diff >= freshCacheTime -> CacheState.NEEDS_SOFT_REFRESH
            else -> CacheState.NEEDS_NO_REFRESH
        }
    }

    override fun delete(request: FutureRequest, data: CacheItem): Boolean {
        val prefs = context.getSharedPreferences(DEFAULT_CACHE_SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(data.url).apply()
        return true
    }

    companion object {
        private const val DEFAULT_CACHE_SHARED_PREFS_NAME = "org.repolizer.cache"
    }
}