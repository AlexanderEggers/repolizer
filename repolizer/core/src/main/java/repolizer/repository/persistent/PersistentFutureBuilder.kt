package repolizer.repository.persistent

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.DatabaseOperation
import repolizer.persistent.CacheItem
import repolizer.repository.future.FutureBuilder

open class PersistentFutureBuilder: FutureBuilder() {

    var cacheOperation: CacheOperation? = null
    var databaseOperation: DatabaseOperation? = null

    var typeToken: TypeToken<*>? = null

    var cacheItem: CacheItem? = null

    var databaseItem: Any? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE database item can be set. Make sure that " +
                        "you don't use more than one @DatabaseBody parameter for this method.")
            } else field = value
        }

    var databaseItemClass: Class<*>? = null
        set(value) {
            if (field != null) {
                throw IllegalStateException("Only ONE database item can be set. Make sure that " +
                        "you don't use more than one @DatabaseBody parameter for this method.")
            } else field = value
        }

    open fun buildCache(repolizer: Repolizer): PersistentCacheFuture {
        return PersistentCacheFuture(repolizer, this)
    }

    open fun buildDatabase(repolizer: Repolizer): PersistentDatabaseFuture {
        return PersistentDatabaseFuture(repolizer, this)
    }
}