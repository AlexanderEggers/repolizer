package repolizer.adapter

abstract class StorageAdapter<T> {

    abstract fun insert(repositoryClass: Class<*>, url: String, sql: String, data: Any, clazz: Class<*>): Boolean

    abstract fun update(repositoryClass: Class<*>, url: String, sql: String, data: Any, clazz: Class<*>)

    abstract fun get(repositoryClass: Class<*>, url: String, sql: String): T

    abstract fun delete(repositoryClass: Class<*>, url: String, sql: String)

    open fun canHaveActiveConnection(): Boolean {
        return false
    }

    open fun <Wrapper> establishConnection(repositoryClass: Class<*>, url: String): Wrapper? {
        //Do nothing by default
        return null
    }
}