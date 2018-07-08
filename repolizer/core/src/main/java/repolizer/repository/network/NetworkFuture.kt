package repolizer.repository.network

import repolizer.Repolizer
import repolizer.repository.future.Future
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.progress.ProgressData
import repolizer.repository.request.RequestType

abstract class NetworkFuture<B>
constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder<B>): Future<B>() {

    val requestType: RequestType = futureBuilder.requestType
            ?: throw IllegalStateException("Request type is null.")

    val headerMap: Map<String, String> = futureBuilder.headerMap
    val queryMap: Map<String, String> = futureBuilder.queryMap

    val fullUrl: String by lazy {
        repolizer.baseUrl?.let { baseUrl ->
            if (baseUrl.substring(baseUrl.length) != "/") {
                "$baseUrl/${futureBuilder.url}"
            } else {
                "$baseUrl${futureBuilder.url}"
            }
        } ?: futureBuilder.url
    }

    protected val progressController: ProgressController? = repolizer.progressController
    protected val loginManager: LoginManager? = repolizer.loginManager

    protected val requiresLogin: Boolean = futureBuilder.requiresLogin
    protected val showProgress: Boolean = futureBuilder.showProgress

    protected val progressData: ProgressData by lazy {
        val lazyProgressData = futureBuilder.progressData ?: ProgressData()
        lazyProgressData.requestType = requestType
        lazyProgressData
    }

    fun execute(): B? {
        onStart()

        val executionType = onDetermineExecutionType()
        val newBody =  when (executionType) {
            ExecutionType.DOWNLOAD_DATA -> {
                if (showProgress) progressController?.internalShow(fullUrl, progressData)

                if (requiresLogin) {
                    val checkSuccessful = checkLogin()
                    if (checkSuccessful) onExecute(executionType)
                    else null
                } else {
                    onExecute(executionType)
                }
            }
            ExecutionType.USE_CACHE -> onExecute(executionType)
        }

        onFinished()
        return newBody
    }

    private fun checkLogin(): Boolean {
        loginManager?.let {
            val isLoginValid = it.isCurrentLoginValid()
            return if (isLoginValid) {
                true
            } else {
                it.onLoginInvalid()
                false
            }
        }
                ?: throw IllegalStateException("Checking the login requires a LoginManager. " +
                        "Use the setter of the Repolizer class to set your custom " +
                        "implementation.")
    }
}