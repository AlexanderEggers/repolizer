package repolizer.adapter.future

import repolizer.adapter.WrapperAdapter
import repolizer.repository.future.Future
import repolizer.repository.future.FutureRequest

class FutureWrapperAdapter : WrapperAdapter<Future<*>>() {

    override fun <B> execute(future: Future<B>, request: FutureRequest): Future<*> {
        return future
    }
}