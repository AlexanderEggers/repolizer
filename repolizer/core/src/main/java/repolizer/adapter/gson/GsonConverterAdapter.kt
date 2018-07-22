package repolizer.adapter.gson

import com.google.gson.Gson
import repolizer.adapter.ConverterAdapter

class GsonConverterAdapter(private val gson: Gson): ConverterAdapter() {

    @Suppress("UNCHECKED_CAST")
    override fun <T> convertStringToData(repositoryClass: Class<*>, data: String, clazz: Class<T>): T? {
        return if(clazz == String::class.java) {
            data as? T
        } else {
            gson.fromJson(data, clazz)
        }
    }

    override fun convertDataToString(repositoryClass: Class<*>, data: Any): String? {
        return gson.toJson(data)
    }
}