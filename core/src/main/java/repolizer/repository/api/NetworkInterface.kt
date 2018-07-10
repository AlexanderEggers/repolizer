package repolizer.repository.api

import android.arch.lifecycle.LiveData
import okhttp3.RequestBody
import repolizer.repository.response.NetworkResponse
import repolizer.repository.util.QueryHashMap
import retrofit2.http.*

interface NetworkInterface {

    @GET("{url}")
    fun get(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap
    ): LiveData<NetworkResponse<String>>

    @POST("{url}")
    fun post(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap,
            @Body raw: RequestBody?
    ): LiveData<NetworkResponse<String>>

    @PUT("{url}")
    fun put(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap,
            @Body raw: RequestBody?
    ): LiveData<NetworkResponse<String>>

    @DELETE("{url}")
    fun delete(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap
    ): LiveData<NetworkResponse<String>>

    @HTTP(method = "DELETE", path = "{url}", hasBody = true)
    fun delete(
            @HeaderMap headerMap: Map<String, String>,
            @Path(value = "url", encoded = true) url: String,
            @QueryMap map: QueryHashMap,
            @Body raw: RequestBody?
    ): LiveData<NetworkResponse<String>>
}