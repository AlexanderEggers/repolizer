package repolizer.repository.response

class NetworkResponse<T>(val body: T?, val url: String, val statusCode: Int, val status: NetworkResponseStatus) {

    fun isSuccessful(): Boolean {
        return status == NetworkResponseStatus.SUCCESS
    }

    fun isError(): Boolean {
        return (status === NetworkResponseStatus.FAILED
                || status === NetworkResponseStatus.NETWORK_ERROR)
    }

    fun <R> withBody(body: R?): NetworkResponse<R> {
        return NetworkResponse(body, url, statusCode, status)
    }
}