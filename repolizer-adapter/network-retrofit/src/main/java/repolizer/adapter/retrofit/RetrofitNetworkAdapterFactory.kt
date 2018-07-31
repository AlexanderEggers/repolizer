package repolizer.adapter.retrofit

import com.google.gson.Gson
import okhttp3.OkHttpClient
import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import repolizer.adapter.retrofit.api.DefaultNetworkController
import repolizer.adapter.retrofit.api.NetworkController
import repolizer.adapter.retrofit.api.NetworkInterface
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.reflect.Type

class RetrofitNetworkAdapterFactory
constructor(baseUrl: String,
            gson: Gson = Gson(),
            httpClient: OkHttpClient? = null,
            networkControllerClass: Class<out NetworkController> = DefaultNetworkController::class.java) : AdapterFactory<RetrofitNetworkAdapter> {

    private var networkController: NetworkController

    init {
        val retrofitBuilder = Retrofit.Builder().apply {
            baseUrl(baseUrl)
            addConverterFactory(ScalarsConverterFactory.create())

            httpClient?.let { client(it) }
        }

        val networkInterface = retrofitBuilder.build()?.create(NetworkInterface::class.java)
        networkController = networkInterface
                ?.let { networkControllerClass.getConstructor(NetworkInterface::class.java, Gson::class.java) }
                ?.newInstance(networkInterface, gson)
                ?: throw IllegalStateException("Internal error: NetworkController is null. " +
                "Usage of reflection to create an instance of your NetworkController failed.")
    }

    override fun get(returnType: Type, repositoryClass: Class<*>, repolizer: Repolizer): RetrofitNetworkAdapter? {
        return RetrofitNetworkAdapter(networkController)
    }
}