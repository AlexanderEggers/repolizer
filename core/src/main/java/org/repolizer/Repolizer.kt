package org.repolizer

import okhttp3.OkHttpClient

class Repolizer internal constructor(builder: Builder){

    init {
        
    }

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

        private var httpClient: OkHttpClient? = null
        private var baseUrl: String? = null

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