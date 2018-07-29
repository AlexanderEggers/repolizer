package repolizer.adapter.util

import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
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
    }
}