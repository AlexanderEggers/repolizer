package repolizer.adapter

import repolizer.repository.future.FutureRequest

abstract class DataAdapter<T> {

    abstract fun insert(request: FutureRequest, converter: ConverterAdapter?, data: Any?): Boolean

    abstract fun update(request: FutureRequest, data: Any?): Boolean

    abstract fun get(request: FutureRequest, converter: ConverterAdapter?): T?

    abstract fun delete(request: FutureRequest): Boolean

    open fun canHaveActiveConnections(): Boolean {
        return false
    }

    open fun <Wrapper> establishConnection(request: FutureRequest): Wrapper? {
        //Do nothing by default
        return null
    }
}