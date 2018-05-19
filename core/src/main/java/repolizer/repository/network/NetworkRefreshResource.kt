package repolizer.repository.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.content.Context
import android.support.annotation.MainThread
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.repository.api.NetworkController
import repolizer.repository.response.NetworkResponse
import repolizer.repository.response.ProgressController
import repolizer.repository.response.ResponseService
import repolizer.repository.util.*

class NetworkRefreshResource<Entity> internal constructor(repolizer: Repolizer, builder: NetworkBuilder<Entity>) {

    private val result = MediatorLiveData<String>()

    private val context: Context = repolizer.context
    private val gson: Gson = repolizer.gson
    private val controller: NetworkController = repolizer.networkController
    private val progressController: ProgressController? = repolizer.progressController
    private val loginManager: LoginManager? = repolizer.loginManager
    private val responseService: ResponseService? = repolizer.responseService
    private val appExecutor: AppExecutor = AppExecutor
    private val updateLayer: NetworkRefreshLayer<Entity> = builder.networkLayer as NetworkRefreshLayer<Entity>

    private val requiresLogin: Boolean = builder.requiresLogin
    private val showProgress: Boolean = builder.showProgress

    private val url: String = builder.url
    private val fullUrl: String = if (repolizer.baseUrl!!.substring(repolizer.baseUrl.length) != "/") {
        repolizer.baseUrl + "/" + builder.url
    } else {
        repolizer.baseUrl + builder.url
    }

    private val requestType: RequestType = builder.requestType!!
    private val bodyType: TypeToken<*> = builder.typeToken!!

    private val headerMap: Map<String, String> = builder.headerMap
    private val queryMap: Map<String, String> = builder.queryMap

    private var callFinishedCallback: FetchSecurityLayer? = null

    @MainThread
    fun execute(fetchSecurityLayer: FetchSecurityLayer): LiveData<String> {
        this.callFinishedCallback = fetchSecurityLayer

        if (fetchSecurityLayer.allowFetch()) {
            val apiResponse = controller.get(headerMap, url, queryMap)

            if (showProgress) {
                progressController?.show(url, requestType)
            }

            if (requiresLogin) {
                checkLogin(apiResponse)
            } else {
                executeCall(apiResponse)
            }
        } else {
            fetchSecurityLayer.onFetchFinished()
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
        result.addSource(networkResponse) { response ->
            result.removeSource(networkResponse)

            var body: Entity? = null
            try {
                body = gson.fromJson<Entity>(response!!.body, bodyType.type)
            } catch (e: Exception) {
                Log.e("Error parsing JSON:\n" + response!!.body + "\n", e.message)
            }

            val objectResponse: NetworkResponse<Entity> = response!!.withBody(body)

            if (showProgress) {
                progressController?.close()
            }

            if (response.isSuccessful() && objectResponse.body != null) {
                responseService?.handleSuccess(response)
                updateLayer.updateDB(objectResponse.body)
                updateLayer.updateFetchTime(Utils.makeUrlId(fullUrl))
            } else {
                responseService?.handleError(response)
            }

            callFinishedCallback!!.onFetchFinished()
            result.value = response.body
        }
    }
}
