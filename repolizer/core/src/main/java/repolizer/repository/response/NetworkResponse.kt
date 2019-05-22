package repolizer.repository.response

class NetworkResponse
constructor(val body: Any?,
            val url: String = "",
            val statusCode: Int = 0,
            val status: NetworkResponseStatus)