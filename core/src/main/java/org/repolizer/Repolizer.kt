package org.repolizer

class Repolizer internal constructor(builder: Builder){

    companion object {

        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {

        fun setClient(): Builder {

        }

        fun setBaseUrl(): Builder {

        }

        fun setProgress(): Builder {

        }

        fun setLoginManager(): Builder {

        }

        fun setResponseService(): Builder {

        }

        fun build() {
            Repolizer(this)
        }
    }
}