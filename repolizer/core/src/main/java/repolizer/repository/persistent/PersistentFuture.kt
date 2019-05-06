package repolizer.repository.persistent

import repolizer.Repolizer
import repolizer.adapter.CacheAdapter
import repolizer.adapter.DataAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.future.Future
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
abstract class PersistentFuture<Body>
constructor(repolizer: Repolizer,
            futureRequest: PersistentFutureRequest) : Future<Body>(repolizer) {

    protected val storageAdapter: DataAdapter<Body>? = AdapterUtil.getSafeAdapter(repolizer.storageAdapters,
            futureRequest.bodyType, futureRequest.repositoryClass, repolizer) as? DataAdapter<Body>
    protected val cacheAdapter: CacheAdapter? = AdapterUtil.getSafeAdapter(repolizer.cacheAdapters,
            futureRequest.bodyType, futureRequest.repositoryClass, repolizer) as? CacheAdapter

    override fun execute(): Body? {
        onStart()
        val result = onExecute(onDetermineExecutionType())
        onFinished(result)
        return result
    }

    override fun onDetermineExecutionType(): ExecutionType {
        return ExecutionType.USE_STORAGE
    }
}