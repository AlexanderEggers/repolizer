package repolizer.adapter.future

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.Type
import repolizer.repository.future.Future

class FutureWrapperAdapterFactory: AdapterFactory<WrapperAdapter<*>> {

    override fun get(returnType: Type, repositoryClass: Class<*>, repolizer: Repolizer): WrapperAdapter<*>? {
        return if(returnType.javaClass != Future::class.java) null
        else FutureWrapperAdapter()
    }
}