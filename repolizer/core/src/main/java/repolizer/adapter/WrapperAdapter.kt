package repolizer.adapter

import repolizer.repository.future.Future
import repolizer.repository.network.NetworkFuture
import java.lang.reflect.Type

interface WrapperAdapter<O> {

    fun <B> execute(future: NetworkFuture<B>): O
}