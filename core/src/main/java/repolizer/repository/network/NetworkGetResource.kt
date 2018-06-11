package repolizer.repository.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.content.Context
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.database.cache.CacheState
import repolizer.repository.api.NetworkController
import repolizer.repository.progress.ProgressController
import repolizer.repository.progress.ProgressData
import repolizer.repository.response.NetworkResponse
import repolizer.repository.response.ResponseService
import repolizer.repository.util.AppExecutor
import repolizer.repository.login.LoginManager
import repolizer.repository.request.RequestType
import repolizer.repository.util.Utils.Companion.makeUrlId
import repolizer.repository.util.Utils.Companion.prepareUrl

class NetworkGetResource<Entity> internal constructor(repolizer: Repolizer, builder: NetworkBuilder<Entity>) {

    private val result = MediatorLiveData<Entity>()

    private val context: Context = repolizer.appContext
    private val gson: Gson = repolizer.gson
    private val controller: NetworkController = repolizer.networkController
    private val progressController: ProgressController? = repolizer.progressController
    private val loginManager: LoginManager? = repolizer.loginManager
    private val responseService: ResponseService? = repolizer.responseService
    private val appExecutor: AppExecutor = AppExecutor
    private val getLayer: NetworkGetLayer<Entity> = builder.networkLayer as? NetworkGetLayer<Entity>
            ?: throw IllegalStateException("Internal error: Network layer is null.")

    private val requiresLogin: Boolean = builder.requiresLogin
    private val showProgress: Boolean = builder.showProgress
    private val deleteIfCacheIsTooOld: Boolean = builder.isDeletingCacheIfTooOld

    private val url: String = builder.url
    private val fullUrl: String = if (repolizer.baseUrl.substring(repolizer.baseUrl.length) != "/") {
        "${repolizer.baseUrl}/${builder.url}"
    } else {
        "${repolizer.baseUrl}${builder.url}"
    }

    private val requestType: RequestType = RequestType.GET
    private val progressData: ProgressData = builder.progressData ?: object : ProgressData() {}
    private val bodyType: TypeToken<*> = builder.typeToken
            ?: throw IllegalStateException("Internal error: Body type is null.")

    private val headerMap: Map<String, String> = builder.headerMap
    private val queryMap: Map<String, String> = builder.queryMap

    private lateinit var fetchSecurityLayer: FetchSecurityLayer
    private lateinit var cacheState: CacheState

    init {
        progressData.requestType = requestType
    }

    @MainThread
    fun execute(fetchSecurityLayer: FetchSecurityLayer, allowFetch: Boolean): LiveData<Entity> {
        this.fetchSecurityLayer = fetchSecurityLayer

        val testSource = loadCache()
        result.addSource(testSource) { data ->
            result.removeSource(testSource)

            val needsFetchByTime = getLayer.needsFetchByTime(makeUrlId(fullUrl))
            result.addSource(needsFetchByTime, { currentCacheState ->
                currentCacheState?.run {
                    result.removeSource(needsFetchByTime)

                    cacheState = this@run
                    val needsFetch = cacheState == CacheState.NEEDS_SOFT_REFRESH ||
                            this@run == CacheState.NEEDS_HARD_REFRESH || this@run == CacheState.NO_CACHE

                    if ((data == null || needsFetch) && allowFetch) {
                        if (fetchSecurityLayer.allowFetch()) {
                            fetchFromNetwork()
                        } else {
                            establishConnection()
                        }
                    } else {
                        fetchSecurityLayer.onFetchFinished()
                        establishConnection()
                    }
                }
            })
        }

        return result
    }

    @MainThread
    private fun fetchFromNetwork() {
        val networkResponse = controller.get(headerMap, prepareUrl(url), queryMap)
        if (showProgress) progressController?.internalShow(url, progressData)

        if (requiresLogin) {
            checkLogin(networkResponse)
        } else {
            executeCall(networkResponse)
        }
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

    private fun executeCall(apiResponse: LiveData<NetworkResponse<String>>) {
        result.addSource(apiResponse) { response ->
            response?.run {
                result.removeSource(apiResponse)

                appExecutor.workerThread.execute {
                    if (showProgress) progressController?.internalClose(fullUrl)

                    if (isSuccessful()) {
                        var objectResponse: NetworkResponse<Entity>? = null

                        try {
                            gson.fromJson<Entity>(body, bodyType.type)?.let {
                                objectResponse = withBody(it)
                            }
                        } catch (e: Exception) {
                            Log.e(NetworkRefreshResource::class.java.name, e.message)
                            e.printStackTrace()
                        }

                        objectResponse?.body?.let {
                            responseService?.handleSuccess(requestType,this@run)
                            getLayer.updateDB(it)
                            getLayer.updateFetchTime(makeUrlId(fullUrl))
                            establishConnection()
                        } ?: run {
                            responseService?.handleGesonError(requestType,this)
                            handleCacheIfTooOld()
                        }
                    } else {
                        responseService?.handleRequestError(requestType,this@run)
                        handleCacheIfTooOld()
                    }

                    fetchSecurityLayer.onFetchFinished()
                }
            }
        }
    }

    @WorkerThread
    private fun handleCacheIfTooOld() {
        if (deleteIfCacheIsTooOld && cacheState == CacheState.NEEDS_HARD_REFRESH) {
            getLayer.removeAllData()
        }
    }

    @MainThread
    private fun establishConnection() {
        appExecutor.mainThread.execute({
            val resultSource = loadCache()
            result.addSource(resultSource) { data -> data?.let { result.value = it } }
        })
    }

    @MainThread
    private fun loadCache(): LiveData<Entity> {
        return getLayer.getData()
    }
}
