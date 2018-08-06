package repolizer.adapter

import java.lang.reflect.Type

abstract class ConverterAdapter {
    abstract fun <T> convertStringToData(repositoryClass: Class<*>, data: String, bodyType: Type): T?
    abstract fun convertDataToString(repositoryClass: Class<*>, data: Any): String?
}