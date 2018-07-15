package repolizer.repository.network

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.adapter.CacheAdapter
import repolizer.adapter.NetworkAdapter
import repolizer.adapter.StorageAdapter
import repolizer.repository.future.Future
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.progress.ProgressData
import repolizer.repository.request.RequestType
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.request.RequestProvider
import repolizer.repository.response.ResponseService
import repolizer.repository.util.QueryHashMap

abstract class NetworkFuture<Body>
constructor(protected val repolizer: Repolizer, futureBuilder: NetworkFutureBuilder): Future<Body>() {

    val requestType: RequestType = futureBuilder.requestType
            ?: throw IllegalStateException("Request type is null.")

    val headerMap: Map<String, String> = futureBuilder.headerMap
    val queryMap: QueryHashMap = futureBuilder.queryMap

    val fullUrl: String by lazy {
        repolizer.baseUrl?.let { baseUrl ->
            if (baseUrl.substring(baseUrl.length) != "/") {
                "$baseUrl/${futureBuilder.url}"
            } else {
                "$baseUrl${futureBuilder.url}"
            }
        } ?: futureBuilder.url
    }

    protected val repositoryClass: Class<*> = futureBuilder.repositoryClass
            ?: throw IllegalStateException("Repository class type is null.")

    protected val bodyType: TypeToken<*> = futureBuilder.typeToken
            ?: throw IllegalStateException("Body type is null.")

    protected val networkAdapter: NetworkAdapter = AdapterUtil.getAdapter(repolizer.networkAdapters,
            bodyType.type, repositoryClass, repolizer) as NetworkAdapter
    protected val storageAdapter: StorageAdapter<Body> = AdapterUtil.getAdapter(repolizer.storageAdapters,
                    bodyType.type, repositoryClass, repolizer) as StorageAdapter<Body>
    protected val cacheAdapter: CacheAdapter = AdapterUtil.getAdapter(repolizer.cacheAdapters,
            bodyType.type, repositoryClass, repolizer) as CacheAdapter

    protected val progressController: ProgressController? = repolizer.progressController
    protected val loginManager: LoginManager? = repolizer.loginManager
    protected val responseService: ResponseService? = repolizer.responseService
    protected val requestProvider: RequestProvider<*>? = repolizer.requestProvider

    protected val requiresLogin: Boolean = futureBuilder.requiresLogin
    protected val showProgress: Boolean = futureBuilder.showProgress

    protected val progressData: ProgressData by lazy {
        val lazyProgressData = futureBuilder.progressData ?: ProgressData()
        lazyProgressData.requestType = requestType
        lazyProgressData
    }

    override fun execute(): Body? {
        onStart()

        val executionType = onDetermineExecutionType()
        val newBody =  when (executionType) {
            ExecutionType.USE_NETWORK -> {
                if (showProgress) progressController?.internalShow(fullUrl, progressData)

                if (requiresLogin) {
                    val checkSuccessful = checkLogin()
                    if (checkSuccessful) onExecute(executionType)
                    else null
                } else {
                    onExecute(executionType)
                }
            }
            ExecutionType.USE_STORAGE -> onExecute(executionType)
            ExecutionType.DO_NOTHING -> null
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

    override fun onFinished() {
        super.onFinished()
        progressController?.internalClose(fullUrl)
    }
}