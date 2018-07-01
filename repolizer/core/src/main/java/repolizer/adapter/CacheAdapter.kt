package repolizer.adapter

import repolizer.persistent.CacheState
import repolizer.repository.network.NetworkGetFuture
import repolizer.repository.persistent.PersistentCacheFuture

interface CacheAdapter {

    fun execute(cacheFuture: PersistentCacheFuture)

    fun get(networkFuture: NetworkGetFuture<*>): CacheState
}