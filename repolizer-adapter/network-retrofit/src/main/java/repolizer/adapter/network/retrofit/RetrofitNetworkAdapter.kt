package repolizer.adapter.network.retrofit

import repolizer.adapter.NetworkAdapter
import repolizer.adapter.network.retrofit.api.NetworkController
import repolizer.repository.network.NetworkFutureRequest
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

class RetrofitNetworkAdapter(private val networkController: NetworkController) : NetworkAdapter() {

    override fun execute(request: NetworkFutureRequest, requestProvider: RequestProvider<*>?): NetworkResponse<String> {
        val url = prepareUrl(request.fullUrl)
        val raw = if (request.rawObjects.isNotEmpty()) request.rawObjects[0] else null

        if (request.rawObjects.size > 1 || request.partObjects.isNotEmpty())
            Logger.getLogger("repolizer").log(Level.SEVERE, "This retrofit adapter implementation " +
                    "only supports one raw body for put/post/delete requests. If you want to want " +
                    "to use multipartbody (therefore to upload more than one object), consider " +
                    "implementing your own network adapter due to the complexity of multipartbody " +
                    "requests. Use @MultipartBody(partName) inside your repositories " +
                    "to provide relevant parameters to your own network implementation.")

        val call = when (request.requestType) {
            RequestType.REFRESH -> networkController.get(request.headerMap, url, request.queryMap)
            RequestType.GET -> networkController.get(request.headerMap, url, request.queryMap)
            RequestType.POST -> networkController.post(request.headerMap, url, request.queryMap,
                    request.rawObjects[0])
            RequestType.PUT -> networkController.put(request.headerMap, url, request.queryMap,
                    raw)
            RequestType.DELETE -> networkController.delete(request.headerMap, url, request.queryMap,
                    raw)
        }

        val requestProviderCast: RequestProvider<Call<String>>? = try {
            val requestType = requestProvider?.getRequestType()
            if (requestType == Call::class.java) {

                @Suppress("UNCHECKED_CAST")
                if (requestType is ParameterizedType
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