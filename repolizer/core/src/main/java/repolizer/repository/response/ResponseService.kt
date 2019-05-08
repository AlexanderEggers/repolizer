package repolizer.repository.response

import repolizer.repository.future.FutureRequest
import repolizer.repository.network.NetworkFutureRequest

abstract class ResponseService {

    private val callbacks: ArrayList<Callback> = ArrayList()

    open fun handleSuccess(request: FutureRequest) {
        callbacks.forEach {
            it.onSuccess(request)
        }
    }

    open fun handleDataError(request: FutureRequest) {
        callbacks.forEach {
            it.onStorageError(request)
        }
    }

    open fun handleCacheError(request: FutureRequest) {
        callbacks.forEach {
            it.onCacheError(request)
        }
    }

    open fun handleRequestError(request: NetworkFutureRequest, response: NetworkResponse?) {
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
        open fun onSuccess(request: FutureRequest) {
            //do nothing by default
        }

        open fun onStorageError(request: FutureRequest) {
            //do nothing by default
        }

        open fun onCacheError(request: FutureRequest) {
            //do nothing by default
        }

        open fun onRequestError(request: NetworkFutureRequest, response: NetworkResponse?) {
            //do nothing by default
        }
    }
}