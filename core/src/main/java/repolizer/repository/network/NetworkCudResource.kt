package repolizer.repository.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.content.Context
import android.support.annotation.MainThread
import repolizer.Repolizer
import repolizer.repository.api.NetworkController
import repolizer.repository.response.NetworkResponse
import repolizer.repository.response.ProgressController
import repolizer.repository.response.ResponseService
import repolizer.repository.util.AppExecutor
import repolizer.repository.util.LoginManager
import repolizer.repository.util.RequestType

class NetworkCudResource<Entity> internal constructor(repolizer: Repolizer, builder: NetworkBuilder<Entity>) {

    private val result = MediatorLiveData<String>()

    private val context: Context = repolizer.context
    private val controller: NetworkController = repolizer.networkController
    private val progressController: ProgressController? = repolizer.progressController
    private val loginManager: LoginManager? = repolizer.loginManager
    private val responseService: ResponseService? = repolizer.responseService
    private val appExecutor: AppExecutor = AppExecutor
    private val cudLayer: NetworkCudLayer<Entity> = builder.networkLayer as NetworkCudLayer

    private val requiresLogin: Boolean = builder.requiresLogin
    private val updateDB: Boolean = builder.updateDB
    private val showProgress: Boolean = builder.showProgress

    private val url: String = builder.url
    private val raw: Entity? = builder.raw
    private val requestType: RequestType = builder.requestType!!

    private val headerMap: Map<String, String> = builder.headerMap
    private val queryMap: Map<String, String> = builder.queryMap

    @MainThread
    fun execute(): LiveData<String> {
        val networkResponse: LiveData<NetworkResponse<String>> = cudLayer.createCall(controller, headerMap,
                url, queryMap, raw)

        if (showProgress) {
            progressController?.show(url, requestType)
        }

        if (requiresLogin) {
            checkLogin(networkResponse)
        } else {
            executeCall(networkResponse)
        }

        return result
    }

    private fun checkLogin(networkResponse: LiveData<NetworkResponse<String>>) {
        appExecutor.workerThread.execute({
            if (loginManager == null) {
                throw IllegalStateException("Checking the login requires a LoginManager. Use the setter" +
                        "of the Repolizer class to set your custom implementation.")
            }

            val isLoginValid = loginManager.isCurrentLoginValid()
            if (isLoginValid) {
                loginManager.onLoginInvalid(context)
            } else {
                executeCall(networkResponse)
            }
        })
    }

    private fun executeCall(networkResponse: LiveData<NetworkResponse<String>>) {
        result.addSource<NetworkResponse<String>>(networkResponse) { response ->
            if (response != null) {
                result.removeSource<NetworkResponse<String>>(networkResponse)

                if (showProgress) {
                    progressController?.close()
                }

                if (response.isSuccessful()) {
                    if (updateDB && raw != null) {
                        cudLayer.updateDB(raw)
                    }
                    responseService?.handleSuccess(response)
                } else {
                    responseService?.handleError(response)
                }

                result.value = response.body
            }
        }
    }
}
