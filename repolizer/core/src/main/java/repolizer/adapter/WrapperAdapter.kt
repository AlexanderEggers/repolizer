package repolizer.adapter

import repolizer.repository.network.NetworkFuture

abstract class WrapperAdapter<W> {

    abstract fun <B> execute(future: NetworkFuture<B>): W

    fun canHaveStorageConnection(): Boolean {
        return false
    }
}