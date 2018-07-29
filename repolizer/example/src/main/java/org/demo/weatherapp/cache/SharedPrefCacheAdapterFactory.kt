package org.demo.weatherapp.cache

import android.content.Context
import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.Type

class SharedPrefCacheAdapterFactory(private val context: Context): AdapterFactory<SharedPrefCacheAdapter> {

    override fun get(returnType: Type, repositoryClass: Class<*>, repolizer: Repolizer): SharedPrefCacheAdapter? {
        return SharedPrefCacheAdapter(context)
    }
}