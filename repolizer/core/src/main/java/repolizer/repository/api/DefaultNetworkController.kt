package repolizer.repository.api

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

class DefaultNetworkController
constructor(networkInterface: NetworkInterface, gson: Gson) : NetworkController(networkInterface, gson) {

    override fun get(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>): Call<String> {
        return super.networkInterface.get(headerMap, url, queryMap)
    }

    override fun post(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): Call<String> {
        return if (rawObject != null) {
            val json = super.gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse("application/json"), json)
            return super.networkInterface.post(headerMap, url, queryMap, requestBody)
        } else {
            super.networkInterface.post(headerMap, url, queryMap, null)
        }
    }

    override fun put(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): Call<String> {
        return if (rawObject != null) {
            val json = super.gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse("application/json"), json)
            return super.networkInterface.put(headerMap, url, queryMap, requestBody)
        } else {
            super.networkInterface.put(headerMap, url, queryMap, null)
        }
    }

    override fun delete(headerMap: Map<String, String>, url: String, queryMap: Map<String, String>, rawObject: Any?): Call<String> {
        return if (rawObject != null) {
            val json = super.gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse("application/json"), json)
            super.networkInterface.delete(headerMap, url, queryMap, requestBody)
        } else {
            super.networkInterface.delete(headerMap, url, queryMap)
        }
    }
}