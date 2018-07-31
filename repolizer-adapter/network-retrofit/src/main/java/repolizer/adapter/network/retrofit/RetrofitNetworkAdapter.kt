package repolizer.adapter.network.retrofit

import repolizer.adapter.NetworkAdapter
import repolizer.adapter.network.retrofit.api.NetworkController
import repolizer.repository.network.NetworkFuture
import repolizer.repository.request.RequestProvider
import repolizer.repository.request.RequestType
import repolizer.repository.response.NetworkResponse
import repolizer.repository.response.NetworkResponseStatus
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.util.logging.Level
import java.util.logging.Logger

class RetrofitNetworkAdapter(private val networkController: NetworkController): NetworkAdapter() {

    override fun execute(networkFuture: NetworkFuture<*>, requestProvider: RequestProvider<*>?): NetworkResponse<String> {
        val url = prepareUrl(networkFuture.fullUrl)

        val call = when(networkFuture.requestType) {
            RequestType.REFRESH -> networkController.get(networkFuture.headerMap, url, networkFuture.queryMap)
            RequestType.GET -> networkController.get(networkFuture.headerMap, url, networkFuture.queryMap)
            RequestType.POST -> networkController.post(networkFuture.headerMap, url, networkFuture.queryMap,
                    networkFuture.rawObject)
            RequestType.PUT -> networkController.put(networkFuture.headerMap, url, networkFuture.queryMap,
                    networkFuture.rawObject)
            RequestType.DELETE -> networkController.delete(networkFuture.headerMap, url, networkFuture.queryMap,
                    networkFuture.rawObject)
        }

        val requestProviderCast: RequestProvider<Call<String>>? = try {
            val requestType = requestProvider?.getRequestType()
            if(requestType == Call::class.java) {

                @Suppress("UNCHECKED_CAST")
                if(requestType is ParameterizedType
                        && requestType.actualTypeArguments[0] == String::class.java) {
                    requestProvider as? RequestProvider<Call<String>>
                }
            }
            null
        } catch (e: ClassCastException) {
            Logger.getLogger("repolizer").log(Level.SEVERE, e.message)
            null
        }

        requestProviderCast?.addRequest(url, call)

        return try {
            val response = call.execute()
            requestProviderCast?.removeRequest(url, call)

            NetworkResponse(
                    if (response.isSuccessful) response.body() else getErrorBody(response),
                    url,
                    response.code(),
                    if (response.isSuccessful) NetworkResponseStatus.SUCCESS else NetworkResponseStatus.FAILED)
        } catch (e: IOException) {
            requestProviderCast?.removeRequest(url, call)
            NetworkResponse(null, url, 0, NetworkResponseStatus.NETWORK_ERROR)
        }
    }

    private fun prepareUrl(url: String): String {
        return url.split("?")[0]
    }

    private fun getErrorBody(response: Response<String>): String {
        return try {
            response.errorBody()?.toString() ?: ""
        } catch (e: IOException) {
            Logger.getLogger("repolizer").log(Level.SEVERE, e.message)
            e.printStackTrace()
            ""
        }
    }
}