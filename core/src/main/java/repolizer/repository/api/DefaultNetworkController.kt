package repolizer.repository.api

import android.arch.lifecycle.LiveData
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import repolizer.repository.response.NetworkResponse

class DefaultNetworkController(networkInterface: NetworkInterface, gson: Gson) : NetworkController(networkInterface, gson) {

    override fun get(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>): LiveData<NetworkResponse<String>> {
        return networkInterface.get(headerMap, url, queryMap)
    }

    override fun post(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): LiveData<NetworkResponse<String>> {
        return if (rawObject != null) {
            val json = gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse("application/json"), json)
            return networkInterface.post(headerMap, url, queryMap, requestBody)
        } else {
            networkInterface.post(headerMap, url, queryMap, null)
        }
    }

    override fun put(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): LiveData<NetworkResponse<String>> {
        return if (rawObject != null) {
            val json = gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse("application/json"), json)
            return networkInterface.put(headerMap, url, queryMap, requestBody)
        } else {
            networkInterface.put(headerMap, url, queryMap, null)
        }
    }

    override fun delete(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): LiveData<NetworkResponse<String>> {
        return if (rawObject != null) {
            val json = gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse("application/json"), json)
            networkInterface.delete(headerMap, url, queryMap, requestBody)
        } else {
            networkInterface.delete(headerMap, url, queryMap)
        }
    }
}