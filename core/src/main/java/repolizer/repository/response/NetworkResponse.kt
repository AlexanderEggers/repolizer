package repolizer.repository.response

class NetworkResponse<T>
internal constructor(val body: T?,
                     val url: String,
                     val statusCode: Int,
                     val status: NetworkResponseStatus) {

    fun isSuccessful(): Boolean {
        return status == NetworkResponseStatus.SUCCESS
    }

    fun <R> withBody(body: R): NetworkResponse<R> {
        return NetworkResponse(body, url, statusCode, status)
    }
}