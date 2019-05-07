package repolizer.adapter.converter.gson

import com.google.gson.Gson
import repolizer.adapter.ConverterAdapter
import java.lang.reflect.Type

class GsonConverterAdapter(val gson: Gson) : ConverterAdapter() {

    override fun <T> convertStringToData(repositoryClass: Class<*>, data: String, bodyType: Type): T? {
        return try {
            gson.fromJson(data, bodyType)
        } catch (e: Exception) {
            null
        }
    }

    override fun convertDataToString(repositoryClass: Class<*>, data: Any): String? {
        return try {
            gson.toJson(data)
        } catch (e: Exception) {
            null
        }
    }
}