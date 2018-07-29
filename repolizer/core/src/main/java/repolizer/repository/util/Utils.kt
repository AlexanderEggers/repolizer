package repolizer.repository.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class Utils {

    companion object {

        fun getGeneratedRepositoryName(repositoryClass: Class<*>): String {
            return "Generated_${repositoryClass.simpleName}"
        }

        fun getBodyType(returnType: Type): Class<*> {
            return if (returnType is ParameterizedType) {
                returnType.actualTypeArguments[0] as Class<*>
            } else {
                returnType as Class<*>
            }
        }
    }
}