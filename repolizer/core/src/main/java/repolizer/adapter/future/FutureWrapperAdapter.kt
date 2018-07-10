package repolizer.adapter.future

import repolizer.adapter.WrapperAdapter
import repolizer.repository.future.Future

class FutureWrapperAdapter: WrapperAdapter<Future<*>>() {

    override fun <B> execute(future: repolizer.repository.future.Future<B>): Future<B> {
        return future
    }
}