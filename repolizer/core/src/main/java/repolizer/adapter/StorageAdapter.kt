package repolizer.adapter

abstract class StorageAdapter<T> {

    lateinit var converterAdapter: ConverterAdapter

    fun init(converterAdapter: ConverterAdapter) {
        this.converterAdapter = converterAdapter
    }

    abstract fun insert(repositoryClass: Class<*>, url: String,
                        sql: String, data: Any): Boolean

    abstract fun update(repositoryClass: Class<*>, url: String, sql: String, data: Any)

    abstract fun get(repositoryClass: Class<*>, url: String, sql: String): T?

    abstract fun delete(repositoryClass: Class<*>, url: String, sql: String)

    open fun canHaveActiveConnections(): Boolean {
        return false
    }

    open fun <Wrapper> establishConnection(repositoryClass: Class<*>, url: String, sql: String): Wrapper? {
        //Do nothing by default
        return null
    }
}