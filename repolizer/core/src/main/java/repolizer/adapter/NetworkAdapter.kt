package repolizer.adapter

import repolizer.repository.network.NetworkFuture
import repolizer.repository.response.NetworkResponse

interface NetworkAdapter {

    fun execute(networkFuture: NetworkFuture<*>): NetworkResponse<String>
}