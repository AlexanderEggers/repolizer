package repolizer.adapter.network.retrofit.api

import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.RequestBody
import repolizer.adapter.network.retrofit.RetrofitAdapterUtils
import repolizer.repository.util.QueryHashMap
import retrofit2.Call

class DefaultNetworkController
constructor(networkInterface: NetworkInterface, gson: Gson) : NetworkController(networkInterface, gson) {

    override fun get(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap): Call<String> {
        return super.networkInterface.get(headerMap, url, queryMap)
    }

    override fun post(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap, rawObject: Any?): Call<String> {
        return if (rawObject != null) {
            val json = super.gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse(DEFAULT_MEDIA_TYPE), json)
            return super.networkInterface.post(headerMap, RetrofitAdapterUtils.prepareUrl(url), queryMap, requestBody)
        } else {
            super.networkInterface.post(headerMap, RetrofitAdapterUtils.prepareUrl(url), queryMap, null)
        }
    }

    override fun put(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap, rawObject: Any?): Call<String> {
        return if (rawObject != null) {
            val json = super.gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse(DEFAULT_MEDIA_TYPE), json)
            return super.networkInterface.put(headerMap, RetrofitAdapterUtils.prepareUrl(url), queryMap, requestBody)
        } else {
            super.networkInterface.put(headerMap, RetrofitAdapterUtils.prepareUrl(url), queryMap, null)
        }
    }

    override fun patch(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap, rawObject: Any?): Call<String> {
        return if (rawObject != null) {
            val json = super.gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse(DEFAULT_MEDIA_TYPE), json)
            return super.networkInterface.patch(headerMap, RetrofitAdapterUtils.prepareUrl(url), queryMap, requestBody)
        } else {
            super.networkInterface.patch(headerMap, RetrofitAdapterUtils.prepareUrl(url), queryMap, null)
        }
    }

    override fun delete(headerMap: Map<String, String>, url: String, queryMap: QueryHashMap, rawObject: Any?): Call<String> {
        return if (rawObject != null) {
            val json = super.gson.toJson(rawObject)
            val requestBody = RequestBody.create(MediaType.parse(DEFAULT_MEDIA_TYPE), json)
            super.networkInterface.delete(headerMap, RetrofitAdapterUtils.prepareUrl(url), queryMap, requestBody)
        } else {
            super.networkInterface.delete(headerMap, RetrofitAdapterUtils.prepareUrl(url), queryMap)
        }
    }

    companion object {
        private const val DEFAULT_MEDIA_TYPE = "application/json"
    }
}