package repolizer.adapter.cache.sharedprefs

import android.content.Context
import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.Type

class SharedPrefCacheAdapterFactory(private val context: Context) : AdapterFactory<SharedPrefCacheAdapter> {

    override fun get(type: Type, repositoryClass: Class<*>, repolizer: Repolizer): SharedPrefCacheAdapter? {
        return SharedPrefCacheAdapter(context)
    }
}