package repolizer.adapter.converter.gson

import com.google.gson.Gson
import repolizer.Repolizer
import repolizer.adapter.ConverterAdapter
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.Type

class GsonConverterAdapterFactory
@JvmOverloads constructor(private val gson: Gson = Gson()) : AdapterFactory<ConverterAdapter> {

    override fun get(type: Type, repositoryClass: Class<*>, repolizer: Repolizer): ConverterAdapter? {
        return GsonConverterAdapter(gson)
    }
}