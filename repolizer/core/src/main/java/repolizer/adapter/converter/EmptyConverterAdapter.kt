package repolizer.adapter.converter

import repolizer.adapter.ConverterAdapter
import java.lang.reflect.Type

class EmptyConverterAdapter: ConverterAdapter() {

    override fun <T> convertStringToData(repositoryClass: Class<*>, data: String, bodyType: Type): T? {
        return null
    }

    override fun convertDataToString(repositoryClass: Class<*>, data: Any): String? {
        return null
    }
}