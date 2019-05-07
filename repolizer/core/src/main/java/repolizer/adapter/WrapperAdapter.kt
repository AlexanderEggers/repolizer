package repolizer.adapter

import repolizer.repository.future.Future
import repolizer.repository.future.FutureRequest
import repolizer.repository.network.NetworkFutureRequest

abstract class WrapperAdapter<W> {

    open fun <B> execute(future: Future<B>, request: FutureRequest): W? {
        return null
    }

    open fun <B> establishDataConnection(future: Future<B>, request: NetworkFutureRequest, dataAdapter: DataAdapter<B>): W? {
        return null
    }

    open fun canHaveDataConnection(): Boolean {
        return false
    }
}