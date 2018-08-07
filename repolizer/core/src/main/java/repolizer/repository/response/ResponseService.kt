package repolizer.repository.response

import repolizer.repository.request.RequestType

abstract class ResponseService {

    private val callbacks: ArrayList<Callback> = ArrayList()

    open fun handleSuccess(requestType: RequestType, response: NetworkResponse<String>) {
        callbacks.forEach {
            it.onSuccess(requestType, response)
        }
    }

    open fun handleStorageError(requestType: RequestType, response: NetworkResponse<String>) {
        callbacks.forEach {
            it.onStorageError(requestType, response)
        }
    }

    open fun handleRequestError(requestType: RequestType, response: NetworkResponse<String>) {
        callbacks.forEach {
            it.onRequestError(requestType, response)
        }
    }

    fun addCallback(callback: Callback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: Callback) {
        callbacks.remove(callback)
    }

    abstract class Callback {
        open fun onSuccess(requestType: RequestType, response: NetworkResponse<String>) {

        }

        open fun onStorageError(requestType: RequestType, response: NetworkResponse<String>) {

        }

        open fun onRequestError(requestType: RequestType, response: NetworkResponse<String>) {

        }
    }
}