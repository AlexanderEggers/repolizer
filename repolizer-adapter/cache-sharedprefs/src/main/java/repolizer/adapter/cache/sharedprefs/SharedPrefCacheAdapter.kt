package repolizer.adapter.cache.sharedprefs

import android.content.Context
import repolizer.adapter.CacheAdapter
import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState

class SharedPrefCacheAdapter(private val context: Context): CacheAdapter() {

    override fun save(repositoryClass: Class<*>, data: CacheItem): Boolean {
        val prefs = context.getSharedPreferences("org.repolizer.cache", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putLong(data.url, data.cacheTime)
        edit.apply()
        return true
    }

    override fun get(repositoryClass: Class<*>, url: String, freshCacheTime: Long, maxCacheTime: Long): CacheState {
        val prefs = context.getSharedPreferences("org.repolizer.cache", Context.MODE_PRIVATE)

        val lastUpdated = prefs.getLong(url, 0L)
        if(lastUpdated == 0L) return CacheState.NO_CACHE

        val diff = System.currentTimeMillis() - lastUpdated

        return when {
            diff >= maxCacheTime -> CacheState.NEEDS_HARD_REFRESH
            diff >= freshCacheTime -> CacheState.NEEDS_SOFT_REFRESH
            else -> CacheState.NEEDS_NO_REFRESH
        }
    }

    override fun delete(repositoryClass: Class<*>, data: CacheItem): Boolean {
        val prefs = context.getSharedPreferences("org.repolizer.cache", Context.MODE_PRIVATE)
        prefs.edit().remove(data.url).apply()
        return true
    }
}