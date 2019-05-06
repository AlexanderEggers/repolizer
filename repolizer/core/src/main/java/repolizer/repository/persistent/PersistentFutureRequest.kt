package repolizer.repository.persistent

import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.DataOperation
import repolizer.repository.future.FutureRequest

open class PersistentFutureRequest(builder: PersistentFutureRequestBuilder) : FutureRequest(builder) {

    var cacheOperation: CacheOperation? = null
    var dataOperation: DataOperation? = null

    var cacheObject = builder.cacheObject
    var dataObject = builder.dataObject
}