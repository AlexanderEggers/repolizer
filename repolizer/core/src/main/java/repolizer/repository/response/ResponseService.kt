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

    open fun handleCacheError(requestType: RequestType, response: NetworkResponse<String>) {
        callbacks.forEach {
            it.onCacheError(requestType, response)
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
        open fun onSuccess(requestType: RequestType, response: NetworkResponse<String>) {
            //do nothing by default
        }

        open fun onStorageError(requestType: RequestType, response: NetworkResponse<String>) {
            //do nothing by default
        }

        open fun onCacheError(requestType: RequestType, response: NetworkResponse<String>) {
            //do nothing by default
        }

        open fun onRequestError(requestType: RequestType, response: NetworkResponse<String>) {
            //do nothing by default
        }
    }
}