package repolizer.repository.persistent

import com.sun.xml.internal.ws.wsdl.writer.document.soap.Body
import repolizer.Repolizer
import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.DataOperation
import repolizer.repository.future.FutureRequestBuilder

open class PersistentFutureRequestBuilder : FutureRequestBuilder() {

    var cacheOperation: CacheOperation? = null
    var dataOperation: DataOperation? = null

    var cacheObject: String? = null
    var dataObject: Any? = null

    open fun buildCache(repolizer: Repolizer): PersistentCacheFuture {
        return PersistentCacheFuture(repolizer, PersistentFutureRequest(this))
    }

    open fun <Body> buildData(repolizer: Repolizer, returnType: Class<Body>): PersistentDataFuture<Body> {
        return PersistentDataFuture(repolizer, PersistentFutureRequest(this))
    }

    open fun <Body> buildDataWithList(repolizer: Repolizer, returnType: Class<Body>): PersistentDataFuture<List<Body>> {
        return PersistentDataFuture(repolizer, PersistentFutureRequest(this))
    }
}