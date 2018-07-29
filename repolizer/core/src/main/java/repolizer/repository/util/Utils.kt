package repolizer.repository.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class Utils {

    companion object {

        fun getGeneratedRepositoryName(repositoryClass: Class<*>): String {
            return "Generated_${repositoryClass.simpleName}"
        }
    }
}