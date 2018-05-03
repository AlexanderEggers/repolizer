package org.repolizer

import android.content.Context
import okhttp3.OkHttpClient

class Repolizer internal constructor(val context: Context, builder: Builder){

    val httpClient: OkHttpClient? = builder.httpClient
    val baseUrl: String? = builder.baseUrl

    fun create(repositoryClass: Class<*>) {

    }

    fun getDatabase(databaseClass: Class<*>) {

    }

    companion object {

        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {

        internal var httpClient: OkHttpClient? = null
        internal var baseUrl: String? = null

        fun setClient(httpClient: OkHttpClient): Builder {
            this.httpClient = httpClient
            return this@Builder
        }

        fun setBaseUrl(baseUrl: String): Builder {
            this.baseUrl = baseUrl
            return this@Builder
        }

        fun setProgress(): Builder {

            return this@Builder
        }

        fun setLoginManager(): Builder {

            return this@Builder
        }

        fun setResponseService(): Builder {

            return this@Builder
        }

        fun build(context: Context): Repolizer {
            return Repolizer(context,this@Builder)
        }
    }
}