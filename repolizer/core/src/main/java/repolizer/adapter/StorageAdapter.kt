package repolizer.adapter

import java.lang.reflect.Type

abstract class StorageAdapter<T> {

    lateinit var converter: ConverterAdapter

    fun init(converter: ConverterAdapter) {
        this.converter = converter
    }

    abstract fun insert(repositoryClass: Class<*>, url: String,
                        sql: String, data: Any, bodyType: Type): Boolean

    abstract fun update(repositoryClass: Class<*>, url: String, sql: String, data: Any?, bodyType: Type)

    abstract fun get(repositoryClass: Class<*>, url: String, sql: String, bodyType: Type): T?

    abstract fun delete(repositoryClass: Class<*>, url: String, sql: String)

    open fun canHaveActiveConnections(): Boolean {
        return false
    }

    open fun <Wrapper> establishConnection(repositoryClass: Class<*>, url: String, sql: String): Wrapper? {
        //Do nothing by default
        return null
    }
}