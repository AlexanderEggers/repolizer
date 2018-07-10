package repolizer.adapter

import repolizer.repository.future.Future

abstract class WrapperAdapter<W> {

    abstract fun <B> execute(future: Future<B>): W

    open fun canHaveStorageConnection(): Boolean {
        return false
    }
}