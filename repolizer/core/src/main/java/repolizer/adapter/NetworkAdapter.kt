package repolizer.adapter

import repolizer.repository.network.NetworkFuture
import repolizer.repository.request.RequestProvider
import repolizer.repository.response.NetworkResponse

abstract class NetworkAdapter {

    abstract fun execute(networkFuture: NetworkFuture<*>, requestProvider: RequestProvider<*>?): NetworkResponse<String>
}