package repolizer.repository.response

import retrofit2.Call

interface RequestProvider {
    fun addRequest(url: String, call: Call<String>)
    fun removeRequest(url: String, call: Call<String>)
    fun cancelRequestByUrl(url: String)
    fun cancelAllRequests()
}