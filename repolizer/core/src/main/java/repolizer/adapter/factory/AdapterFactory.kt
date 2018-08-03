package repolizer.adapter.factory

import repolizer.Repolizer
import java.lang.reflect.Type

interface AdapterFactory<A> {
    fun get(type: Type, repositoryClass: Class<*>, repolizer: Repolizer): A?
}