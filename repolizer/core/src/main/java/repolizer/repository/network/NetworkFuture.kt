package repolizer.repository.network

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.adapter.CacheAdapter
import repolizer.adapter.NetworkAdapter
import repolizer.adapter.StorageAdapter
import repolizer.adapter.WrapperAdapter
import repolizer.repository.future.Future
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.progress.ProgressData
import repolizer.repository.request.RequestType
import repolizer.adapter.util.AdapterUtil

abstract class NetworkFuture<Body>
constructor(private val repolizer: Repolizer, futureBuilder: NetworkFutureBuilder<Body>): Future<Body>() {

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

    private val bodyType: TypeToken<*> = futureBuilder.typeToken
            ?: throw IllegalStateException("Internal error: Body type is null.")

    protected val networkAdapter: NetworkAdapter = AdapterUtil.getAdapter(repolizer.networkAdapters,
            bodyType.type, String::class.java, repolizer) as NetworkAdapter
    protected val storageAdapter: StorageAdapter<Body> = AdapterUtil.getAdapter(repolizer.storageAdapters,
                    bodyType.type, String::class.java, repolizer) as StorageAdapter<Body>
    protected val cacheAdapter: CacheAdapter = AdapterUtil.getAdapter(repolizer.cacheAdapters,
            bodyType.type, String::class.java, repolizer) as CacheAdapter

    protected val progressController: ProgressController? = repolizer.progressController
    protected val loginManager: LoginManager? = repolizer.loginManager

    protected val requiresLogin: Boolean = futureBuilder.requiresLogin
    protected val showProgress: Boolean = futureBuilder.showProgress

    protected val progressData: ProgressData by lazy {
        val lazyProgressData = futureBuilder.progressData ?: ProgressData()
        lazyProgressData.requestType = requestType
        lazyProgressData
    }

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, bodyType.type,
                String::class.java, repolizer) as WrapperAdapter<Wrapper>

        return if(wrapperAdapter.canHaveStorageConnection() && storageAdapter.canHaveActiveConnection()) {
            storageAdapter.establishConnection(String::class.java, fullUrl)
                    ?: throw IllegalStateException("If you want to use an active storage connection, " +
                            "you need to implement the establishConnection() function inside your " +
                            "StorageAdapter.")
        } else wrapperAdapter.execute(this)
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