package repolizer.repository.persistent

import repolizer.repository.future.FutureBuilder

open class PersistentFutureBuilder: FutureBuilder() {

    var persistentLayer: PersistentLayer? = null

    fun buildCache(): PersistentCacheFuture {
        return PersistentCacheFuture(this)
    }
}