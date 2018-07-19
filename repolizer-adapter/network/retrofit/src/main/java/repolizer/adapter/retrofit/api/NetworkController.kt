package repolizer.adapter.retrofit.api

import com.google.gson.Gson
import repolizer.repository.response.NetworkResponse
import repolizer.repository.util.QueryHashMap
import retrofit2.Call

abstract class NetworkController
constructor(protected val networkInterface: NetworkInterface, protected val gson: Gson) {

    abstract fun get(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap): Call<String>

    abstract fun post(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap, rawObject: Any?): Call<String>

    abstract fun put(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap, rawObject: Any?): Call<String>

    abstract fun delete(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap, rawObject: Any?): Call<String>
}