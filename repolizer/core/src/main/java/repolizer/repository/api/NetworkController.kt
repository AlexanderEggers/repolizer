package repolizer.repository.api

import com.google.gson.Gson
import retrofit2.Call

abstract class NetworkController
constructor(protected val networkInterface: NetworkInterface, protected val gson: Gson) {

    abstract fun get(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>): Call<String>

    abstract fun post(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): Call<String>

    abstract fun put(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): Call<String>

    abstract fun delete(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): Call<String>
}