package repolizer.repository.response

interface ResponseService {
    fun handleSuccess(requestType: RequestType, response: NetworkResponse<String>)
    fun handleGesonError(requestType: RequestType, response: NetworkResponse<String>)
    fun handleRequestError(requestType: RequestType, response: NetworkResponse<String>)
}