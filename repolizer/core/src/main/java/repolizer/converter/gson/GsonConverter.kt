package repolizer.converter.gson

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import repolizer.converter.Converter

class GsonConverter<T>(val gson: Gson): Converter<T>() {

    @Suppress("UNCHECKED_CAST")
    override fun convertStringToData(repositoryClass: Class<*>, data: String): T? {
        return gson.fromJson(data, object: TypeToken<T>() {}.type)
    }

    override fun convertDataToString(repositoryClass: Class<*>, data: Any): String? {
        return gson.toJson(data)
    }
}