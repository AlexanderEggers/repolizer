package repolizer.adapter

import repolizer.converter.Converter

abstract class StorageAdapter<T> {

    lateinit var converter: Converter<T>

    fun init(converter: Converter<T>) {
        this.converter = converter
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