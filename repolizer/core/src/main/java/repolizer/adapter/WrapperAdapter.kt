package repolizer.adapter

import repolizer.repository.future.Future
import repolizer.repository.future.FutureRequest
import repolizer.repository.network.NetworkFutureRequest

abstract class WrapperAdapter<W> {

    open fun <B> execute(future: Future<B>, request: FutureRequest): W? {
        return null
    }

    open fun <B> establishStorageConnection(future: Future<B>, request: NetworkFutureRequest, storageAdapter: StorageAdapter<B>): W? {
        return null
    }

    open fun canHaveStorageConnection(): Boolean {
        return false
    }
}