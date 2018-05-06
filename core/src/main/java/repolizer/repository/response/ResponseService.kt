package repolizer.repository.response

interface ResponseService {

    fun handleSuccess(response: NetworkResponse<String>?)
    fun handleError(response: NetworkResponse<String>?)
}