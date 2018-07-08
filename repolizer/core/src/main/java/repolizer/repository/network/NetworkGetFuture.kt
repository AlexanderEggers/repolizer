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
import repolizer.persistent.CacheState
import repolizer.repository.api.NetworkController
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.progress.ProgressData
import repolizer.repository.request.RequestType
import repolizer.repository.response.NetworkResponse
import repolizer.repository.response.ResponseService
import repolizer.repository.util.AppExecutor
import repolizer.repository.util.Utils.Companion.makeUrlId
import repolizer.repository.util.Utils.Companion.prepareUrl
import retrofit2.Call

class NetworkGetFuture<E>
constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder<E>): NetworkFuture<E>(repolizer, futureBuilder) {

    private val responseService: ResponseService? = repolizer.responseService
    private val deleteIfCacheIsTooOld: Boolean = futureBuilder.isDeletingCacheIfTooOld
    private var allowFetch: Boolean = false

    private val gson: Gson = repolizer.gson
    private val bodyType: TypeToken<*> = futureBuilder.typeToken
            ?: throw IllegalStateException("Internal error: Body type is null.")

    private lateinit var fetchSecurityLayer: FetchSecurityLayer
    private lateinit var cacheState: CacheState

    override fun onDetermineExecutionType(): ExecutionType {
        val cacheData: E? = loadCache() //TODO
        val cacheState = CacheState.NO_CACHE //TODO
        val needsFetch = cacheState == CacheState.NEEDS_SOFT_REFRESH ||
                cacheState == CacheState.NEEDS_HARD_REFRESH || cacheState == CacheState.NO_CACHE

        return if ((cacheData == null || needsFetch) && allowFetch) {
            if (fetchSecurityLayer.allowFetch()) {
                ExecutionType.DOWNLOAD_DATA
            } else {
                ExecutionType.USE_CACHE
            }
        } else {
            ExecutionType.USE_CACHE
        }
    }

    override fun onExecute(executionType: ExecutionType): E? {

    }

    override fun onFinished() {
        super.onFinished()
        fetchSecurityLayer.onFetchFinished()
    }

    private fun onExecuteCall() {
        result.addSource(apiResponse) { response ->
            response?.run {
                result.removeSource(apiResponse)

                appExecutor.workerThread.execute {
                    if (showProgress) progressController?.internalClose(fullUrl)

                    if (isSuccessful()) {
                        var objectResponse: NetworkResponse<Entity>? = null

                        try {
                            gson.fromJson<E>(body, bodyType.type)?.let {
                                objectResponse = withBody(it)
                            }
                        } catch (e: Exception) {
                            Log.e(NetworkRefreshFuture::class.java.name, e.message)
                            e.printStackTrace()
                        }

                        objectResponse?.body?.let {
                            responseService?.handleSuccess(requestType, this@run)
                            getLayer.updateDB(it)
                            getLayer.updateFetchTime(makeUrlId(fullUrl))
                            establishConnection()
                        } ?: run {
                            responseService?.handleGesonError(requestType, this)
                            handleCacheIfTooOld()
                        }
                    } else {
                        responseService?.handleRequestError(requestType, this@run)
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
        appExecutor.mainThread.execute {
            val resultSource = loadCache()
            result.addSource(resultSource) { data -> data?.let { result.value = it } }
        }
    }

    @MainThread
    private fun loadCache(): LiveData<E> {
        return getLayer.getData()
    }
}
