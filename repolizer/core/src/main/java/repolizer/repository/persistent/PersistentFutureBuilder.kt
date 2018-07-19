package repolizer.repository.persistent

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.StorageOperation
import repolizer.persistent.CacheItem
import repolizer.repository.future.FutureBuilder

open class PersistentFutureBuilder: FutureBuilder() {

    var cacheOperation: CacheOperation? = null
    var storageOperation: StorageOperation? = null

    var cacheItem: CacheItem? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE cache item can be set. Make sure that " +
                        "you don't use more than one CacheItem parameter for this method.")
            } else field = value
        }

    var storageItem: Any? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE database item can be set. Make sure that " +
                        "you don't use more than one @StorageBody parameter for this method.")
            } else field = value
        }

    var storageItemClass: Class<*>? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE storage item can be set. Make sure that " +
                        "you don't use more than one @StorageBody parameter for this method.")
            } else field = value
        }

    open fun buildCache(repolizer: Repolizer): PersistentCacheFuture {
        return PersistentCacheFuture(repolizer, this)
    }

    open fun buildStorage(repolizer: Repolizer): PersistentStorageFuture {
        return PersistentStorageFuture(repolizer, this)
    }
}