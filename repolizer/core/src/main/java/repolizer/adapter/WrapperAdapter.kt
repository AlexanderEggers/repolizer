package repolizer.adapter

import repolizer.repository.future.Future

abstract class WrapperAdapter<W> {

    abstract fun <B> execute(future: Future<B>): W

    open fun <B> execute(future: Future<B>, storageAdapter: StorageAdapter<B>,
                         repositoryClass: Class<*>, url: String, sql: String): W? {
        return null
    }

    open fun canHaveStorageConnection(): Boolean {
        return false
    }
}