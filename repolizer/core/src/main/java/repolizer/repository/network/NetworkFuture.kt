package repolizer.repository.network

import repolizer.Repolizer
import repolizer.adapter.CacheAdapter
import repolizer.adapter.ConverterAdapter
import repolizer.adapter.NetworkAdapter
import repolizer.adapter.StorageAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.future.Future
import repolizer.repository.login.LoginManager
import repolizer.repository.request.RequestProvider
import repolizer.repository.response.ResponseService

@Suppress("UNCHECKED_CAST")
abstract class NetworkFuture<Body>
constructor(repolizer: Repolizer,
            futureRequest: NetworkFutureRequest) : Future<Body>(repolizer) {

    protected val networkAdapter: NetworkAdapter? = if (futureRequest.url.isNotEmpty()) {
        AdapterUtil.getAdapter(repolizer.networkAdapters, futureRequest.bodyType,
                futureRequest.repositoryClass, repolizer) as? NetworkAdapter?
    } else null
    protected val converterAdapter: ConverterAdapter? = AdapterUtil.getSafeAdapter(repolizer.converterAdapters,
            futureRequest.bodyType, futureRequest.repositoryClass, repolizer) as? ConverterAdapter
    protected val storageAdapter: StorageAdapter<Body>?
    protected val cacheAdapter: CacheAdapter?

    protected val loginManager: LoginManager? = repolizer.loginManager
    protected val responseService: ResponseService? = repolizer.responseService
    protected val requestProvider: RequestProvider<*>? = repolizer.requestProvider

    protected val requiresLogin: Boolean = futureRequest.requiresLogin
    protected val saveData: Boolean = futureRequest.saveData

    init {
        if (saveData) {
            storageAdapter = AdapterUtil.getAdapter(repolizer.storageAdapters,
                    futureRequest.bodyType, futureRequest.repositoryClass, repolizer) as StorageAdapter<Body>

            cacheAdapter = AdapterUtil.getSafeAdapter(repolizer.cacheAdapters,
                    futureRequest.bodyType, futureRequest.repositoryClass, repolizer) as? CacheAdapter?
        } else {
            storageAdapter = null
            cacheAdapter = null
        }
    }

    override fun execute(): Body? {
        onStart()

        val newBody = when (val executionType = onDetermineExecutionType()) {
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