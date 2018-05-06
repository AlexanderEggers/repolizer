package repolizer.repository.response

import retrofit2.Call

interface RequestProvider {
    fun addRequest(call: Call<String>)
    fun removeRequest(call: Call<String>)
    fun cancelAllRequests()
}