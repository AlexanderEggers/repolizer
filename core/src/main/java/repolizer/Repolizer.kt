package repolizer

import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import repolizer.database.provider.GlobalDatabaseProvider
import repolizer.repository.api.DefaultNetworkController
import repolizer.repository.api.NetworkController
import repolizer.repository.api.NetworkInterface
import repolizer.repository.login.LoginManager
import repolizer.repository.progress.ProgressController
import repolizer.repository.provider.GlobalRepositoryProvider
import repolizer.repository.response.RequestProvider
import repolizer.repository.response.ResponseService
import repolizer.repository.retrofit.LiveDataCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class Repolizer private constructor(val appContext: Context, builder: Builder) {

    val httpClient: OkHttpClient? = builder.httpClient
    val requestProvider: RequestProvider? = builder.requestProvider

    val baseUrl: String = builder.baseUrl
            ?: throw IllegalStateException("Internal error: Base url required.")
    val networkController: NetworkController
    val gson: Gson = builder.gson

    val progressController: ProgressController? = builder.progressController
    val loginManager: LoginManager? = builder.loginManager
    val responseService: ResponseService? = builder.responseService

    init {
        val retrofitBuilder = Retrofit.Builder().apply {
            baseUrl(baseUrl)
            addConverterFactory(ScalarsConverterFactory.create())
            addCallAdapterFactory(LiveDataCallAdapterFactory(requestProvider))

            httpClient?.let { client(it) }
        }

        val networkInterface = retrofitBuilder.build()?.create(NetworkInterface::class.java)
        networkController = networkInterface
                ?.let { builder.networkControllerClass.getConstructor(NetworkInterface::class.java, Gson::class.java) }
                ?.newInstance(networkInterface, gson)
                ?: throw IllegalStateException("Internal error: NetworkController is null. " +
                "Usage of reflection to create an instance of your NetworkController failed.")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getRepository(repositoryClass: Class<*>): T {
        return GlobalRepositoryProvider.getRepository(this, repositoryClass) as? T
                ?: throw IllegalStateException("Internal error: Repository is null. Make sure " +
                        "that you used the correct class for the function Repolizer.create(Class<*>).")
    }

    fun getDatabase(databaseClass: Class<*>): RoomDatabase {
        return GlobalDatabaseProvider.getDatabase(appContext, databaseClass)
                ?: throw IllegalStateException("Internal error: Database is null. Make sure " +
                        "that you used the correct class for the function Repolizer.getDatabase(Class<*>).")
    }

    companion object {

        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {
        var httpClient: OkHttpClient? = null
            private set
        var requestProvider: RequestProvider? = null
            private set

        var baseUrl: String? = null
            private set
        var networkControllerClass: Class<out NetworkController> = DefaultNetworkController::class.java
            private set
        var gson: Gson = Gson()
            private set

        var progressController: ProgressController? = null
            private set
        var loginManager: LoginManager? = null
            private set
        var responseService: ResponseService? = null
            private set

        fun setClient(httpClient: OkHttpClient): Builder {
            this@Builder.httpClient = httpClient
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

        fun setNetworkConroller(networkControllerClass: Class<NetworkController>): Builder {
            this@Builder.networkControllerClass = networkControllerClass
            return this@Builder
        }

        fun setRequestProvider(requestProvider: RequestProvider): Builder {
            this@Builder.requestProvider = requestProvider
            return this@Builder
        }

        fun build(appContext: Context): Repolizer {
            return Repolizer(appContext, this@Builder)
        }
    }
}