package repolizer.adapter.livedata

import android.arch.lifecycle.LiveData
import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.Type

class LiveDataWrapperFactory: AdapterFactory<LiveDataWrapper> {

    override fun get(returnType: Type, repositoryClass: Class<*>, repolizer: Repolizer): LiveDataWrapper? {
        return if (returnType != LiveData::class.java) {
            null
        } else {
            LiveDataWrapper()
        }
    }
}