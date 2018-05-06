package repolizer.repository.api

import android.arch.lifecycle.LiveData
import com.google.gson.Gson
import repolizer.repository.response.NetworkResponse

abstract class NetworkController(protected val networkInterface: NetworkInterface, protected val gson: Gson) {

    abstract fun get(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>): LiveData<NetworkResponse<String>>

    abstract fun post(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): LiveData<NetworkResponse<String>>

    abstract fun put(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): LiveData<NetworkResponse<String>>

    abstract fun delete(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): LiveData<NetworkResponse<String>>

    abstract fun prepareUrl(url: String): String
}