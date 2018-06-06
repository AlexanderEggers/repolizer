package repolizer.repository.response

class NetworkResponse<T>
internal constructor(val body: T?,
                     val url: String,
                     val statusCode: Int,
                     val status: NetworkResponseStatus) {

    internal fun isSuccessful(): Boolean {
        return status == NetworkResponseStatus.SUCCESS
    }

    internal fun <R> withBody(body: R?): NetworkResponse<R> {
        return NetworkResponse(body, url, statusCode, status)
    }
}