package repolizer.repository.network

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.adapter.CacheAdapter
import repolizer.adapter.ConverterAdapter
import repolizer.adapter.NetworkAdapter
import repolizer.adapter.StorageAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.future.Future
import repolizer.repository.login.LoginManager
import repolizer.repository.request.RequestProvider
import repolizer.repository.request.RequestType
import repolizer.repository.response.ResponseService
import repolizer.repository.util.QueryHashMap
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
abstract class NetworkFuture<Body>
constructor(protected val repolizer: Repolizer,
            protected val futureRequest: NetworkFutureRequest) : Future<Body>(repolizer) {

    val requestType: RequestType = futureRequest.requestType
            ?: throw IllegalStateException("Request type is null.")

    val headerMap: Map<String, String> = futureRequest.headerMap
    val queryMap: QueryHashMap = futureRequest.queryMap
    val rawObjects: List<Any?> = futureRequest.rawObjects
    val partObjects: List<Any?> = futureRequest.partObjects

    val fullUrl: String by lazy {
        repolizer.baseUrl?.let { baseUrl ->
            "$baseUrl${futureRequest.url}"
        } ?: futureRequest.url
    }

    protected val requestUrl: String = futureRequest.url

    protected val repositoryClass: Class<*> = futureRequest.repositoryClass
            ?: throw IllegalStateException("Repository class type is null.")

    protected val wrapperType: TypeToken<*> = futureRequest.typeToken
            ?: throw IllegalStateException("Wrapper type is null.")
    protected val bodyType: Type = futureRequest.bodyType
            ?: throw IllegalStateException("Body type is null.")

    protected val networkAdapter: NetworkAdapter?
    protected val converterAdapter: ConverterAdapter? = AdapterUtil.getSafeAdapter(repolizer.converterAdapters,
            bodyType, repositoryClass, repolizer) as? ConverterAdapter
    protected val storageAdapter: StorageAdapter<Body>?
    protected val cacheAdapter: CacheAdapter?

    protected val loginManager: LoginManager? = repolizer.loginManager
    protected val responseService: ResponseService? = repolizer.responseService
    protected val requestProvider: RequestProvider<*>? = repolizer.requestProvider

    protected val requiresLogin: Boolean = futureRequest.requiresLogin
    protected val saveData: Boolean = futureRequest.saveData

    init {
        networkAdapter = if (requestUrl.isNotEmpty()) {
            AdapterUtil.getAdapter(repolizer.networkAdapters,
                    bodyType, repositoryClass, repolizer) as? NetworkAdapter?
        } else null

        if (saveData) {
            storageAdapter = AdapterUtil.getAdapter(repolizer.storageAdapters,
                    bodyType, repositoryClass, repolizer) as StorageAdapter<Body>

            cacheAdapter = AdapterUtil.getSafeAdapter(repolizer.cacheAdapters,
                    bodyType, repositoryClass, repolizer) as? CacheAdapter?
        } else {
            storageAdapter = null
            cacheAdapter = null
        }
    }

    override fun execute(): Body? {
        onStart()

        val executionType = onDetermineExecutionType()
        val newBody = when (executionType) {
            ExecutionType.USE_NETWORK -> {
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

        onFinished(newBody)
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