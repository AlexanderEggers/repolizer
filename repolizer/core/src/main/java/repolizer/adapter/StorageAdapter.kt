package repolizer.adapter

import java.lang.reflect.Type

abstract class StorageAdapter<T> {

    abstract fun insert(repositoryClass: Class<*>, converter: ConverterAdapter?, url: String,
                        sql: String, data: Any, bodyType: Type): Boolean

    abstract fun update(repositoryClass: Class<*>, sql: String, data: Any?): Boolean

    abstract fun get(repositoryClass: Class<*>, converter: ConverterAdapter?, url: String,
                     sql: String, bodyType: Type): T?

    abstract fun delete(repositoryClass: Class<*>, url: String, sql: String): Boolean

    open fun canHaveActiveConnections(): Boolean {
        return false
    }

    open fun <Wrapper> establishConnection(repositoryClass: Class<*>, url: String, sql: String): Wrapper? {
        //Do nothing by default
        return null
    }
}