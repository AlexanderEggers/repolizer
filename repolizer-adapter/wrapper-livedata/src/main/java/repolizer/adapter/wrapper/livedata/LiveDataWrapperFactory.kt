package repolizer.adapter.wrapper.livedata

import android.arch.lifecycle.LiveData
import archtree.helper.AppExecutor
import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataWrapperFactory(private val appExecutor: AppExecutor = AppExecutor()): AdapterFactory<LiveDataWrapper> {

    override fun get(type: Type, repositoryClass: Class<*>, repolizer: Repolizer): LiveDataWrapper? {
        return when {
            type !is ParameterizedType -> null
            type.rawType != LiveData::class.java -> null
            else -> LiveDataWrapper(appExecutor)
        }
    }
}