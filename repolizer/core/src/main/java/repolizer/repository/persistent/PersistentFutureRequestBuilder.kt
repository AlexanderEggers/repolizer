package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.DataOperation
import repolizer.persistent.CacheItem
import repolizer.repository.future.FutureRequestBuilder

open class PersistentFutureRequestBuilder : FutureRequestBuilder() {

    var cacheOperation: CacheOperation? = null
    var storageOperation: DataOperation? = null

    var cacheItem: CacheItem? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE cache item can be set. Make sure that " +
                        "you don't use more than one @CacheBody parameter for this method.")
            } else field = value
        }

    var storageItem: Any? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE database item can be set. Make sure that " +
                        "you don't use more than one @StorageBody parameter for this method.")
            } else field = value
        }

    open fun buildCache(repolizer: Repolizer): PersistentCacheFuture {
        return PersistentCacheFuture(repolizer, PersistentFutureRequest(this))
    }

    open fun buildStorage(repolizer: Repolizer): PersistentStorageFuture {
        return PersistentStorageFuture(repolizer, PersistentFutureRequest(this))
    }
}