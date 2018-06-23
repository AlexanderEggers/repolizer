package repolizer.repository.request

import retrofit2.Call

interface RequestProvider {
    fun addRequest(url: String, call: Call<String>)
    fun removeRequest(url: String, call: Call<String>)
    fun cancelAllRequests()
}