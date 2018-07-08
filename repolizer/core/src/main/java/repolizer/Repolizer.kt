package repolizer

import com.google.gson.Gson
import repolizer.adapter.ImageAdapter
import repolizer.adapter.NetworkAdapter
import repolizer.adapter.StorageAdapter
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.factory.AdapterFactory
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.provider.GlobalRepositoryProvider
import repolizer.repository.request.RequestProvider
import repolizer.repository.response.ResponseService

class Repolizer private constructor(builder: Builder) {

    val baseUrl: String = builder.baseUrl
            ?: throw IllegalStateException("Internal error: Base url required.")
    val gson: Gson = builder.gson

    val requestProvider: RequestProvider? = builder.requestProvider
    val progressController: ProgressController? = builder.progressController
    val loginManager: LoginManager? = builder.loginManager
    val responseService: ResponseService? = builder.responseService

    val wrapperAdapters: ArrayList<AdapterFactory<WrapperAdapter<*>>> = builder.wrapperAdapters
    val imageAdapters: ArrayList<AdapterFactory<ImageAdapter<*>>> = builder.imageAdapters
    val networkAdapters: ArrayList<AdapterFactory<NetworkAdapter>> = builder.networkAdapters
    val storageAdapters: ArrayList<AdapterFactory<StorageAdapter<*>>> = builder.storageAdapters

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
        val wrapperAdapters: ArrayList<AdapterFactory<WrapperAdapter<*>>> = ArrayList() //TODO Add default wrapper for Future
        val imageAdapters: ArrayList<AdapterFactory<ImageAdapter<*>>> = ArrayList()
        val networkAdapters: ArrayList<AdapterFactory<NetworkAdapter>> = ArrayList()
        val storageAdapters: ArrayList<AdapterFactory<StorageAdapter<*>>> = ArrayList()

        var requestProvider: RequestProvider? = null
            private set

        var baseUrl: String? = null
            private set
        var gson: Gson = Gson()
            private set

        var progressController: ProgressController? = null
            private set
        var loginManager: LoginManager? = null
            private set
        var responseService: ResponseService? = null
            private set

        fun addWrapperAdapter(wrapperAdapter: AdapterFactory<WrapperAdapter<*>>): Builder {
            wrapperAdapters.add(wrapperAdapter)
            return this@Builder
        }

        fun addImageAdapter(imageAdapter: AdapterFactory<ImageAdapter<*>>): Builder {
            imageAdapters.add(imageAdapter)
            return this@Builder
        }

        fun addNetworkAdapter(networkAdapter: AdapterFactory<NetworkAdapter>): Builder {
            networkAdapters.add(networkAdapter)
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

        fun setGson(gson: Gson): Builder {
            this@Builder.gson = gson
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

        fun setRequestProvider(requestProvider: RequestProvider): Builder {
            this@Builder.requestProvider = requestProvider
            return this@Builder
        }

        fun build(): Repolizer {
            return Repolizer(this@Builder)
        }
    }
}