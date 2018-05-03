package org.repolizer

import android.content.Context
import okhttp3.OkHttpClient

class Repolizer internal constructor(builder: Builder){

    val context: Context = builder.context
    val httpClient: OkHttpClient? = builder.httpClient
    val baseUrl: String? = builder.baseUrl

    fun create(repositoryClass: Class<*>) {

    }

    fun getDatabase(databaseClass: Class<*>) {

    }

    companion object {

        fun newBuilder(context: Context): Builder {
            return Builder(context)
        }
    }

    class Builder(internal var context: Context) {

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

        fun build(): Repolizer {
            return Repolizer(this@Builder)
        }
    }
}