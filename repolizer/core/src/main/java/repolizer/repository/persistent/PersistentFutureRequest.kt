package repolizer.repository.persistent

import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.StorageOperation
import repolizer.persistent.CacheItem
import repolizer.repository.future.FutureRequest

open class PersistentFutureRequest(builder: PersistentFutureRequestBuilder) : FutureRequest(builder) {

    var cacheOperation: CacheOperation = builder.cacheOperation
            ?: throw IllegalStateException("CacheOperation is null.")
    var storageOperation: StorageOperation = builder.storageOperation
            ?: throw IllegalStateException("StorageOperation is null.")

    var cacheItem: CacheItem = builder.cacheItem
            ?: throw IllegalStateException("CacheItem is null.")
    var storageItem: Any? = builder.storageItem
}