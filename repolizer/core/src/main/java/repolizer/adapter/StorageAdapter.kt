package repolizer.adapter

abstract class StorageAdapter<T> {

    abstract fun save(repositoryClass: Class<*>, url: String, data: String)

    abstract fun get(repositoryClass: Class<*>, url: String): T

    abstract fun delete(repositoryClass: Class<*>, url: String, deleteAll: Boolean)

    fun canHaveActiveConnection(): Boolean {
        return false
    }

    fun <Wrapper> establishConnection(repositoryClass: Class<*>, url: String): Wrapper? {
        //Do nothing by default
        return null
    }
}