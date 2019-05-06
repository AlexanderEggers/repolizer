package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.DataOperation
import repolizer.repository.future.FutureRequestBuilder

open class PersistentFutureRequestBuilder : FutureRequestBuilder() {

    var cacheOperation: CacheOperation? = null
    var dataOperation: DataOperation? = null

    var cacheObject: Any? = null
    var dataObject: Any? = null

    open fun buildCache(repolizer: Repolizer): PersistentCacheFuture {
        return PersistentCacheFuture(repolizer, PersistentFutureRequest(this))
    }

    open fun buildData(repolizer: Repolizer): PersistentStorageFuture {
        return PersistentStorageFuture(repolizer, PersistentFutureRequest(this))
    }
}