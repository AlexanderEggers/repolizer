package repolizer.adapter

import repolizer.repository.network.NetworkFuture

interface WrapperAdapter<B, O> {

    fun execute(future: NetworkFuture<B>): O
}