package repolizer.adapter

import repolizer.repository.network.NetworkFutureRequest
import repolizer.repository.request.RequestProvider
import repolizer.repository.response.NetworkResponse

abstract class NetworkAdapter {

    abstract fun execute(request: NetworkFutureRequest, requestProvider: RequestProvider<*>?): NetworkResponse?
}