package repolizer.repository.response

import repolizer.repository.future.FutureRequest
import repolizer.repository.request.RequestType

abstract class ResponseService {

    private val callbacks: ArrayList<Callback> = ArrayList()

    open fun handleSuccess(requestType: RequestType, request: FutureRequest) {
        callbacks.forEach {
            it.onSuccess(requestType, request)
        }
    }

    open fun handleStorageError(requestType: RequestType, request: FutureRequest) {
        callbacks.forEach {
            it.onStorageError(requestType, request)
        }
    }

    open fun handleCacheError(requestType: RequestType, request: FutureRequest) {
        callbacks.forEach {
            it.onCacheError(requestType, request)
        }
    }

    open fun handleRequestError(requestType: RequestType, request: FutureRequest, response: NetworkResponse<String>?) {
        callbacks.forEach {
            it.onRequestError(requestType, request, response)
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
        open fun onSuccess(requestType: RequestType, request: FutureRequest) {
            //do nothing by default
        }

        open fun onStorageError(requestType: RequestType, request: FutureRequest) {
            //do nothing by default
        }

        open fun onCacheError(requestType: RequestType, request: FutureRequest) {
            //do nothing by default
        }

        open fun onRequestError(requestType: RequestType, request: FutureRequest, response: NetworkResponse<String>?) {
            //do nothing by default
        }
    }
}