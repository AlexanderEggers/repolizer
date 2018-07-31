package repolizer.adapter.livedata

import android.arch.lifecycle.LiveData
import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataWrapperFactory: AdapterFactory<LiveDataWrapper> {

    override fun get(returnType: Type, repositoryClass: Class<*>, repolizer: Repolizer): LiveDataWrapper? {
        return when {
            returnType !is ParameterizedType -> null
            returnType.rawType != LiveData::class.java -> null
            else -> LiveDataWrapper()
        }
    }
}