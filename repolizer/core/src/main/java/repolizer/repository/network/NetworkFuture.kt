package repolizer.repository.network

import repolizer.Repolizer
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.progress.ProgressData
import repolizer.repository.request.RequestType
import repolizer.repository.util.AppExecutor

abstract class NetworkFuture<B>
constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder<B>) {

    val requestType: RequestType = futureBuilder.requestType
            ?: throw IllegalStateException("Request type is null.")

    protected val progressController: ProgressController? = repolizer.progressController
    protected val loginManager: LoginManager? = repolizer.loginManager
    protected val appExecutor: AppExecutor = AppExecutor

    protected val url: String = futureBuilder.url
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
                if (showProgress) progressController?.internalShow(url, progressData)

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

    protected abstract fun onExecute(executionType: ExecutionType): B?

    protected abstract fun onDetermineExecutionType(): ExecutionType

    protected open fun onCreate() {

    }

    protected open fun onStart() {

    }

    protected open fun onFinished() {

    }

    private fun checkLogin(): Boolean {
        loginManager?.let {
            val isLoginValid = it.isCurrentLoginValid()
            return if (isLoginValid) {
                true
            } else {
                appExecutor.mainThread.execute {
                    it.onLoginInvalid()
                }
                false
            }
        }
                ?: throw IllegalStateException("Checking the login requires a LoginManager. " +
                        "Use the setter of the Repolizer class to set your custom " +
                        "implementation.")
    }
}