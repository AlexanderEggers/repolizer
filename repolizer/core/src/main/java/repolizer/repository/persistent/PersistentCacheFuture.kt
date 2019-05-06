package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.WrapperAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.annotation.repository.util.CacheOperation
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
class PersistentCacheFuture
constructor(private val repolizer: Repolizer,
            private val futureRequest: PersistentFutureRequest) : PersistentFuture<Boolean>(repolizer, futureRequest) {

    override fun <Wrapper> create(): Wrapper {
        val wrapperAdapter = AdapterUtil.getAdapter(repolizer.wrapperAdapters, futureRequest.typeToken.type,
                futureRequest.repositoryClass, repolizer) as WrapperAdapter<Wrapper>
        return wrapperAdapter.execute(this, futureRequest)
                ?: throw IllegalStateException("It seems like that your WrapperAdapter does not" +
                        "have the method execute() implemented.")
    }

    override fun onStart() {
        if (cacheAdapter == null) { throw IllegalStateException("Cache adapter is null.") }
    }

    override fun onExecute(executionType: ExecutionType): Boolean? {
        return when (futureRequest.cacheOperation) {
            CacheOperation.INSERT -> cacheAdapter?.save(futureRequest, futureRequest.cacheItem)
            CacheOperation.DELETE -> cacheAdapter?.delete(futureRequest, futureRequest.cacheItem)
        }
    }
}