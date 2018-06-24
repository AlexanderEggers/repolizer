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
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.progress.ProgressData
import repolizer.repository.response.NetworkResponse
import repolizer.repository.request.RequestType
import repolizer.repository.response.ResponseService
import repolizer.repository.util.*
import repolizer.repository.util.Utils.Companion.makeUrlId
import repolizer.repository.util.Utils.Companion.prepareUrl

class NetworkRefreshFuture<Entity> constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder<Entity>) {

    private val result = MediatorLiveData<Boolean>()

    private val context: Context = repolizer.appContext
    private val gson: Gson = repolizer.gson
    private val controller: NetworkController = repolizer.networkController
    private val progressController: ProgressController? = repolizer.progressController
    private val loginManager: LoginManager? = repolizer.loginManager
    private val responseService: ResponseService? = repolizer.responseService
    private val appExecutor: AppExecutor = AppExecutor
    private val updateLayer: NetworkRefreshLayer<Entity> = futureBuilder.networkLayer as? NetworkRefreshLayer<Entity>
            ?: throw IllegalStateException("Internal error: Network layer is null.")

    private val requiresLogin: Boolean = futureBuilder.requiresLogin
    private val showProgress: Boolean = futureBuilder.showProgress

    private val url: String = futureBuilder.url
    private val fullUrl: String = if (repolizer.baseUrl.substring(repolizer.baseUrl.length) != "/") {
        repolizer.baseUrl + "/" + futureBuilder.url
    } else {
        repolizer.baseUrl + futureBuilder.url
    }

    private val requestType: RequestType = RequestType.REFRESH
    private val progressData: ProgressData = futureBuilder.progressData ?: object: ProgressData() {}
    private val bodyType: TypeToken<*> = futureBuilder.typeToken
            ?: throw IllegalStateException("Internal error: Body type is null.")

    private val headerMap: Map<String, String> = futureBuilder.headerMap
    private val queryMap: Map<String, String> = futureBuilder.queryMap

    private lateinit var fetchSecurityLayer: FetchSecurityLayer

    init {
        progressData.requestType = requestType
    }

    @MainThread
    fun execute(fetchSecurityLayer: FetchSecurityLayer): LiveData<Boolean> {
        this.fetchSecurityLayer = fetchSecurityLayer

        if (fetchSecurityLayer.allowFetch()) {
            val apiResponse = controller.get(headerMap, prepareUrl(url), queryMap)

            if (showProgress) {
                progressController?.internalShow(url, progressData)
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
        loginManager?.let {
            result.addSource(it.isCurrentLoginValid(), { isLoginValid ->
                isLoginValid?.run {
                    if (this@run) {
                        executeCall(networkResponse)
                    } else {
                        appExecutor.mainThread.execute {
                            it.onLoginInvalid(context)
                        }
                    }
                }
            })
        }
                ?: throw IllegalStateException("Checking the login requires a LoginManager. " +
                        "Use the setter of the Repolizer class to set your custom " +
                        "implementation.")
    }

    private fun executeCall(networkResponse: LiveData<NetworkResponse<String>>) {
        result.addSource(networkResponse) { response ->
            response?.run {
                result.removeSource(networkResponse)

                appExecutor.workerThread.execute {
                    if (showProgress) progressController?.internalClose(fullUrl)

                    if (isSuccessful()) {
                        var objectResponse: NetworkResponse<Entity>? = null

                        try {
                            gson.fromJson<Entity>(body, bodyType.type)?.let {
                                objectResponse = withBody(it)
                            }
                        } catch (e: Exception) {
                            Log.e(NetworkRefreshFuture::class.java.name, e.message)
                            e.printStackTrace()
                        }

                        objectResponse?.body?.let {
                            responseService?.handleSuccess(requestType,this@run)
                            updateLayer.updateDB(it)
                            updateLayer.updateFetchTime(makeUrlId(fullUrl))
                            result.postValue(true)
                        } ?: responseService?.handleGesonError(requestType,this@run)
                    } else {
                        responseService?.handleRequestError(requestType,this@run)
                    }

                    fetchSecurityLayer.onFetchFinished()
                }
            }
        }
    }
}
