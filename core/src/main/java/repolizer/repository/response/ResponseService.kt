package repolizer.repository.response

interface ResponseService {
    fun handleSuccess(response: NetworkResponse<String>)
    fun handleGesonError(response: NetworkResponse<String>)
    fun handleRequestError(response: NetworkResponse<String>)
}