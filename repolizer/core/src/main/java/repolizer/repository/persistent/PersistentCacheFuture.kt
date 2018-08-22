package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.CacheOperation
import repolizer.persistent.CacheItem
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
class PersistentCacheFuture
constructor(repolizer: Repolizer, futureBuilder: PersistentFutureBuilder) : PersistentFuture<Boolean>(repolizer, futureBuilder) {

    private val cacheOperation: CacheOperation = futureBuilder.cacheOperation
            ?: throw IllegalStateException("CacheOperation is null.")
    private val cacheItem: CacheItem = futureBuilder.cacheItem
            ?: throw IllegalStateException("CacheItem is null.")

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, wrapperType.type,
                repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this)
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        return when (cacheOperation) {
            CacheOperation.INSERT -> cacheAdapter?.save(repositoryClass, cacheItem)
            CacheOperation.DELETE -> cacheAdapter?.delete(repositoryClass, cacheItem)
        }
    }
}