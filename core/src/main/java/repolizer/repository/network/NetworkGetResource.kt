package repolizer.repository.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.content.Context
import android.support.annotation.MainThread
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.database.cache.CacheState
import repolizer.repository.api.NetworkController
import repolizer.repository.response.NetworkResponse
import repolizer.repository.response.ProgressController
import repolizer.repository.response.ResponseService
import repolizer.repository.util.AppExecutor
import repolizer.repository.util.FetchSecurityLayer
import repolizer.repository.util.LoginManager
import repolizer.repository.util.RequestType
import repolizer.repository.util.Utils.Companion.makeUrlId

class NetworkGetResource<Entity> internal constructor(repolizer: Repolizer, builder: NetworkBuilder<Entity>) {

    private val result = MediatorLiveData<Entity>()

    private val context: Context = repolizer.context
    private val gson: Gson = repolizer.gson
    private val controller: NetworkController = repolizer.networkController
    private val progressController: ProgressController? = repolizer.progressController
    private val loginManager: LoginManager? = repolizer.loginManager
    private val responseService: ResponseService? = repolizer.responseService
    private val appExecutor: AppExecutor = AppExecutor
    private val getLayer: NetworkGetLayer<Entity> = builder.networkLayer as NetworkGetLayer<Entity>

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

    private lateinit var fetchSecurityLayer: FetchSecurityLayer
    private lateinit var cacheState: CacheState

    @MainThread
    fun execute(fetchSecurityLayer: FetchSecurityLayer, allowFetch: Boolean): LiveData<Entity> {
        this.fetchSecurityLayer = fetchSecurityLayer

        val testSource = loadCache()
        result.addSource(testSource) { data ->
            result.removeSource(testSource)

            val needsFetchByTime = getLayer.needsFetchByTime(makeUrlId(fullUrl))
            result.addSource(needsFetchByTime, { cacheState ->
                result.removeSource(needsFetchByTime)

                this@NetworkGetResource.cacheState = cacheState!!
                val needsFetch = cacheState == CacheState.NEEDS_SOFT_REFRESH ||
                        cacheState == CacheState.NEEDS_HARD_REFRESH || cacheState == CacheState.NO_CACHE

                if ((data != null || needsFetch) && allowFetch) {
                    if (fetchSecurityLayer.allowFetch()) {
                        fetchFromNetwork()
                    } else {
                        establishConnection()
                    }
                } else {
                    fetchSecurityLayer.onFetchFinished()
                    establishConnection()
                }
            })
        }

        return result
    }

    @MainThread
    private fun fetchFromNetwork() {
        val networkResponse = controller.get(headerMap, url, queryMap)

        if (showProgress) {
            progressController?.show(url, requestType)
        }

        if (requiresLogin) {
            checkLogin(networkResponse)
        } else {
            executeCall(networkResponse)
        }
    }

    private fun checkLogin(networkResponse: LiveData<NetworkResponse<String>>) {
        appExecutor.workerThread.execute({
            if (loginManager == null) {
                throw IllegalStateException("Checking the login requires a LoginManager. Use the setter" +
                        "of the Repolizer class to set your custom implementation.")
            }

            result.addSource(loginManager.isCurrentLoginValid(), { isLoginValid ->
                if (isLoginValid != null) {
                    if (isLoginValid) {
                        loginManager.onLoginInvalid(context)
                    } else {
                        executeCall(networkResponse)
                    }
                }
            })
        })
    }

    private fun executeCall(apiResponse: LiveData<NetworkResponse<String>>) {
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)

            appExecutor.workerThread.execute({
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

                if (objectResponse.isSuccessful() && objectResponse.body != null) {
                    responseService?.handleSuccess(response)
                    getLayer.updateDB(objectResponse.body)
                    getLayer.updateFetchTime(makeUrlId(fullUrl))
                } else {
                    responseService?.handleError(response)
                    if (cacheState == CacheState.NEEDS_HARD_REFRESH) {
                        getLayer.removeAllData()
                    }
                }

                fetchSecurityLayer.onFetchFinished()
                establishConnection()
            })
        }
    }

    @MainThread
    private fun establishConnection() {
        appExecutor.mainThread.execute({
            val resultSource = loadCache()
            result.addSource(resultSource) { data -> result.value = data }
        })
    }

    @MainThread
    private fun loadCache(): LiveData<Entity> {
        return getLayer.getData()
    }
}
