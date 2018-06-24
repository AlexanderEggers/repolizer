package repolizer.repository.persistent

class PersistentFutureBuilder {

    var persistentLayer: PersistentLayer? = null

    fun build(): PersistentFuture {
        return PersistentFuture(this)
    }
}