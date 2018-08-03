package repolizer.adapter.wrapper.livedata

import android.arch.lifecycle.LiveData
import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataWrapperFactory: AdapterFactory<LiveDataWrapper> {

    override fun <T: Type> get(bodyType: T, repositoryClass: Class<*>, repolizer: Repolizer): LiveDataWrapper? {
        return when {
            bodyType !is ParameterizedType -> null
            bodyType.rawType != LiveData::class.java -> null
            else -> LiveDataWrapper()
        }
    }
}