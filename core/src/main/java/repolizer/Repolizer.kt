package repolizer

import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import repolizer.database.provider.GlobalDatabaseProvider
import repolizer.repository.BaseRepository
import repolizer.repository.api.DefaultJsonNetworkController
import repolizer.repository.api.NetworkController
import repolizer.repository.api.NetworkInterface
import repolizer.repository.provider.GlobalRepositoryProvider
import repolizer.repository.response.ProgressController
import repolizer.repository.response.RequestProvider
import repolizer.repository.response.ResponseService
import repolizer.repository.retrofit.LiveDataCallAdapterFactory
import repolizer.repository.util.LoginManager
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class Repolizer private constructor(val context: Context, builder: Builder) {

    private val httpClient: OkHttpClient? = builder.httpClient
    private val requestProvider: RequestProvider? = builder.requestProvider

    val baseUrl: String? = builder.baseUrl
    val networkController: NetworkController
    var gson: Gson = builder.gson

    val progressController: ProgressController? = builder.progressController
    val loginManager: LoginManager? = builder.loginManager
    val responseService: ResponseService? = builder.responseService

    init {
        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(baseUrl!!)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory(requestProvider))

        if (httpClient != null) {
            retrofitBuilder.client(httpClient)
        }

        val networkInterface = retrofitBuilder.build().create(NetworkInterface::class.java)
        networkController = builder.networkControllerClass.getConstructor(
                NetworkInterface::class.java, Gson::class.java).newInstance(networkInterface, gson)
    }

    fun <T: BaseRepository<*>> create(repositoryClass: Class<T>): T {
        return GlobalRepositoryProvider.getRepository(this, repositoryClass)
    }

    fun <T: RoomDatabase> getDatabase(databaseClass: Class<T>): T {
        return GlobalDatabaseProvider.getDatabase(context, databaseClass)
    }

    companion object {

        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {

        internal var httpClient: OkHttpClient? = null
        internal var baseUrl: String? = null
        internal var gson: Gson = Gson()
        internal var networkControllerClass: Class<out NetworkController> = DefaultJsonNetworkController::class.java

        internal var progressController: ProgressController? = null
        internal var loginManager: LoginManager? = null
        internal var responseService: ResponseService? = null
        internal var requestProvider: RequestProvider? = null

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

        fun build(context: Context): Repolizer {
            return Repolizer(context, this@Builder)
        }
    }
}