package repolizer.repository.network

import android.arch.lifecycle.LiveData
import android.support.annotation.MainThread
import repolizer.repository.api.NetworkController
import repolizer.repository.response.NetworkResponse

interface NetworkCudLayer<Entity> : NetworkLayer<Entity> {

    @MainThread
    fun createCall(controller: NetworkController,
                   headerMap: Map<String, String>,
                   url: String,
                   queryMap: Map<String, String>,
                   raw: Any?): LiveData<NetworkResponse<String>>
}
