package repolizer.repository.network

import android.arch.lifecycle.LiveData
import android.support.annotation.MainThread
import repolizer.repository.api.NetworkController
import repolizer.repository.response.NetworkResponse
import repolizer.repository.util.QueryHashMap

interface NetworkCudLayer<Entity> : NetworkLayer<Entity> {

    @MainThread
    fun createCall(controller: NetworkController,
                   headerMap: Map<String, String>,
                   url: String,
                   queryMap: QueryHashMap,
                   raw: Entity?): LiveData<NetworkResponse<String>>
}
