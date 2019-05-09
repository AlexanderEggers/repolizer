package repolizer.repository.persistent

import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.DataOperation
import repolizer.repository.future.FutureRequest

open class PersistentFutureRequest(builder: PersistentFutureRequestBuilder) : FutureRequest(builder) {

    var cacheOperation: CacheOperation? = builder.cacheOperation
    var dataOperation: DataOperation? = builder.dataOperation

    var returnStatement: String? = builder.returnStatement
    var overrideEmptyReturnStatement: Boolean = builder.overrideEmptyReturnStatement

    var cacheObject = builder.cacheObject
    var dataObject = builder.dataObject
}