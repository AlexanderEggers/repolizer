package repolizer.repository.response

import repolizer.repository.request.RequestType

interface ResponseService {
    fun handleSuccess(requestType: RequestType, response: NetworkResponse<String>)
    fun handleGesonError(requestType: RequestType, response: NetworkResponse<String>)
    fun handleRequestError(requestType: RequestType, response: NetworkResponse<String>)
}