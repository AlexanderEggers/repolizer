package repolizer.adapter.converter

import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.Type

class EmptyConverterAdapterFactory: AdapterFactory<EmptyConverterAdapter> {

    override fun get(type: Type, repositoryClass: Class<*>, repolizer: Repolizer): EmptyConverterAdapter? {
        return EmptyConverterAdapter()
    }
}