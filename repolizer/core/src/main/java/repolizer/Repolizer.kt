package repolizer

import repolizer.adapter.*
import repolizer.adapter.factory.AdapterFactory
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.provider.GlobalRepositoryProvider
import repolizer.repository.request.RequestProvider
import repolizer.repository.response.ResponseService

class Repolizer private constructor(builder: Builder) {

    val baseUrl: String? = builder.baseUrl

    val requestProvider: RequestProvider<*>? = builder.requestProvider
    val progressController: ProgressController? = builder.progressController
    val loginManager: LoginManager? = builder.loginManager
    val responseService: ResponseService? = builder.responseService

    val wrapperAdapters: ArrayList<AdapterFactory<WrapperAdapter<*, *>>> = builder.wrapperAdapters
    val networkAdapters: ArrayList<AdapterFactory<NetworkAdapter>> = builder.networkAdapters
    val storageAdapters: ArrayList<AdapterFactory<StorageAdapter<*>>> = builder.storageAdapters
    val cacheAdapters: ArrayList<AdapterFactory<CacheAdapter>> = builder.cacheAdapters

    @Suppress("UNCHECKED_CAST")
    fun <T> getRepository(repositoryClass: Class<T>): T {
        return GlobalRepositoryProvider.getRepository(this, repositoryClass) as? T
                ?: throw IllegalStateException("Internal error: Repository is null. Make sure " +
                        "that you used the correct class for the function Repolizer.getRepository(...).")
    }

    companion object {

        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {
        val wrapperAdapters: ArrayList<AdapterFactory<WrapperAdapter<*, *>>> = ArrayList() //TODO Add default wrapper for Future
        val networkAdapters: ArrayList<AdapterFactory<NetworkAdapter>> = ArrayList()
        val storageAdapters: ArrayList<AdapterFactory<StorageAdapter<*>>> = ArrayList()
        val cacheAdapters: ArrayList<AdapterFactory<CacheAdapter>> = ArrayList()

        var requestProvider: RequestProvider<*>? = null
            private set

        var baseUrl: String? = null
            private set

        var progressController: ProgressController? = null
            private set
        var loginManager: LoginManager? = null
            private set
        var responseService: ResponseService? = null
            private set

        fun addWrapperAdapter(wrapperAdapter: AdapterFactory<WrapperAdapter<*, *>>): Builder {
            wrapperAdapters.add(wrapperAdapter)
            return this@Builder
        }

        fun addNetworkAdapter(networkAdapter: AdapterFactory<NetworkAdapter>): Builder {
            networkAdapters.add(networkAdapter)
            return this@Builder
        }

        fun addCacheAdapter(cacheAdapter: AdapterFactory<CacheAdapter>): Builder {
            cacheAdapters.add(cacheAdapter)
            return this@Builder
        }

        fun addPersistentAdapter(storageAdapter: AdapterFactory<StorageAdapter<*>>): Builder {
            storageAdapters.add(storageAdapter)
            return this@Builder
        }

        fun setBaseUrl(baseUrl: String): Builder {
            this@Builder.baseUrl = baseUrl
            return this@Builder
        }

        fun setProgress(progressController: ProgressController): Builder {
            this@Builder.progressController = progressController
            return this@Builder
        }

        fun setLoginManager(loginManager: LoginManager): Builder {
            this@Builder.loginManager = loginManager
            return this@Builder
        }

        fun setResponseService(responseService: ResponseService): Builder {
            this@Builder.responseService = responseService
            return this@Builder
        }

        fun setRequestProvider(requestProvider: RequestProvider<*>): Builder {
            this@Builder.requestProvider = requestProvider
            return this@Builder
        }

        fun build(): Repolizer {
            return Repolizer(this@Builder)
        }
    }
}