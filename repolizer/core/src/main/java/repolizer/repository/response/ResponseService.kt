package repolizer.repository.response

import repolizer.repository.network.NetworkFutureRequest
import repolizer.repository.request.RequestType

abstract class ResponseService {

    private val callbacks: ArrayList<Callback> = ArrayList()

    open fun handleSuccess(request: NetworkFutureRequest) {
        callbacks.forEach {
            it.onSuccess(request)
        }
    }

    open fun handleStorageError(request: NetworkFutureRequest) {
        callbacks.forEach {
            it.onStorageError(request)
        }
    }

    open fun handleCacheError(request: NetworkFutureRequest) {
        callbacks.forEach {
            it.onCacheError(request)
        }
    }

    open fun handleRequestError(request: NetworkFutureRequest, response: NetworkResponse<String>?) {
        callbacks.forEach {
            it.onRequestError(request, response)
        }
    }

    fun addCallback(callback: Callback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: Callback) {
        val iterator = callbacks.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()

            if (item == callback) {
                iterator.remove()
                break
            }
        }
    }

    abstract class Callback {
        open fun onSuccess(request: NetworkFutureRequest) {
            //do nothing by default
        }

        open fun onStorageError(request: NetworkFutureRequest) {
            //do nothing by default
        }

        open fun onCacheError(request: NetworkFutureRequest) {
            //do nothing by default
        }

        open fun onRequestError(request: NetworkFutureRequest, response: NetworkResponse<String>?) {
            //do nothing by default
        }
    }
}