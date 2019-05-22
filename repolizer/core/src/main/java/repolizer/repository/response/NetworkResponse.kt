package repolizer.repository.response

class NetworkResponse
constructor(val body: Any?,
            val status: NetworkResponseStatus,
            val url: String = "",
            val statusCode: Int = 0) {

    fun isSuccessful(): Boolean {
        return status == NetworkResponseStatus.SUCCESS
    }
}