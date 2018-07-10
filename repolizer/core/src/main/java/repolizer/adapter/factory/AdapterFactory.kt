package repolizer.adapter.factory

import repolizer.Repolizer
import java.lang.reflect.Type

interface AdapterFactory<T> {
    fun get(returnType: Type, repositoryClass: Class<*>, repolizer: Repolizer): T?
}