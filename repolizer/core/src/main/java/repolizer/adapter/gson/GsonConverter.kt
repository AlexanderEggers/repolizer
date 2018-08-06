package repolizer.adapter.gson

import com.google.gson.Gson
import repolizer.adapter.ConverterAdapter
import java.lang.reflect.Type

class GsonConverter(val gson: Gson): ConverterAdapter() {

    override fun <T> convertStringToData(repositoryClass: Class<*>, data: String, bodyType: Type): T? {
        return gson.fromJson(data, bodyType)
    }

    override fun convertDataToString(repositoryClass: Class<*>, data: Any): String? {
        return gson.toJson(data)
    }
}