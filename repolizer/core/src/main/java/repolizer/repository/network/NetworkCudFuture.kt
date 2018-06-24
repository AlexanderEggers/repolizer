package repolizer.repository.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.content.Context
import android.support.annotation.MainThread
import repolizer.Repolizer
import repolizer.repository.api.NetworkController
import repolizer.repository.response.NetworkResponse
import repolizer.repository.progress.ProgressController
import repolizer.repository.progress.ProgressData
import repolizer.repository.response.ResponseService
import repolizer.repository.util.AppExecutor
import repolizer.repository.login.LoginManager
import repolizer.repository.request.RequestType
import repolizer.repository.util.Utils.Companion.prepareUrl

class NetworkCudFuture<Entity> constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder<Entity>) {

    private val result = MediatorLiveData<String>()

    private val context: Context = repolizer.appContext
    private val controller: NetworkController = repolizer.networkController
    private val progressController: ProgressController? = repolizer.progressController
    private val loginManager: LoginManager? = repolizer.loginManager
    private val responseService: ResponseService? = repolizer.responseService
    private val appExecutor: AppExecutor = AppExecutor
    private val cudLayer: NetworkCudLayer<Entity> = futureBuilder.networkLayer as? NetworkCudLayer<Entity>
            ?: throw IllegalStateException("Internal error: Network layer is null.")

    private val requiresLogin: Boolean = futureBuilder.requiresLogin
    private val showProgress: Boolean = futureBuilder.showProgress

    private val url: String = futureBuilder.url
    private val raw: Entity? = futureBuilder.raw

    private val requestType: RequestType = futureBuilder.requestType
            ?: throw IllegalStateException("Internal error: Request type is null.")
    private val progressData: ProgressData = futureBuilder.progressData ?: object: ProgressData() {}

    private val headerMap: Map<String, String> = futureBuilder.headerMap
    private val queryMap: Map<String, String> = futureBuilder.queryMap

    init {
        progressData.requestType = requestType
    }

    @MainThread
    fun execute(): LiveData<String> {
        val networkResponse: LiveData<NetworkResponse<String>> = cudLayer.createCall(controller,
                headerMap, prepareUrl(url), queryMap, raw)

        if (showProgress) progressController?.internalShow(url, progressData)

        if (requiresLogin) {
            checkLogin(networkResponse)
        } else {
            executeCall(networkResponse)
        }

        return result
    }

    private fun checkLogin(networkResponse: LiveData<NetworkResponse<String>>) {
        loginManager?.let {
            result.addSource(it.isCurrentLoginValid()) { isLoginValid ->
                isLoginValid?.run {
                    if (this@run) {
                        executeCall(networkResponse)
                    } else {
                        appExecutor.mainThread.execute {
                            it.onLoginInvalid(context)
                        }
                    }
                }
            }
        }
                ?: throw IllegalStateException("Checking the login requires a LoginManager. " +
                        "Use the setter of the Repolizer class to set your custom " +
                        "implementation.")
    }

    private fun executeCall(networkResponse: LiveData<NetworkResponse<String>>) {
        result.addSource<NetworkResponse<String>>(networkResponse) { response ->
            response?.run {
                result.removeSource<NetworkResponse<String>>(networkResponse)

                if (showProgress) progressController?.internalClose(url)

                if (isSuccessful()) {
                    responseService?.handleSuccess(requestType,this@run)
                    result.value = body
                } else {
                    responseService?.handleRequestError(requestType,this@run)
                }
            }
        }
    }
}
