package repolizer.adapter.util

import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class AdapterUtil {

    companion object {

        fun <T : AdapterFactory<*>> getAdapter(list: List<T>, returnType: Type, repositoryClass: Class<*>, repolizer: Repolizer): Any {
            var adapter: Any? = null

            for (i in 0 until list.size) {
                if (adapter == null) {
                    adapter = list[i].get(returnType, repositoryClass, repolizer)
                } else break
            }

            if (adapter == null) {
                throw IllegalArgumentException("Cannot find adapter.")
            } else {
                return adapter
            }
        }

        fun getBodyType(returnType: Type): Class<*> {
            return if (returnType is ParameterizedType) {
                return getBodyType(returnType.actualTypeArguments[0])
            } else {
                returnType as Class<*>
            }
        }

        fun hasListType(returnType: Type): Boolean {
            return if (returnType is ParameterizedType) {
                val type = returnType.actualTypeArguments[0]
                if(type is ParameterizedType) {
                    return type.rawType as Class<*> == List::class.java
                }
                return false
            } else {
                false
            }
        }
    }
}