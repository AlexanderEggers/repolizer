package repolizer.repository.persistent

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.adapter.CacheAdapter
import repolizer.adapter.StorageAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.future.Future
import repolizer.repository.network.ExecutionType
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
abstract class PersistentFuture<Body>
constructor(protected val repolizer: Repolizer, futureBuilder: PersistentFutureBuilder) : Future<Body>(repolizer) {

    protected val repositoryClass: Class<*> = futureBuilder.repositoryClass
            ?: throw IllegalStateException("Repository class type is null.")
    protected val wrapperType: TypeToken<*> = futureBuilder.typeToken
            ?: throw IllegalStateException("Wrapper type is null.")
    protected val bodyType: Type = futureBuilder.bodyType
            ?: throw IllegalStateException("Body type is null.")

    protected val storageAdapter: StorageAdapter<Body>? = AdapterUtil.getSafeAdapter(repolizer.storageAdapters,
            bodyType, repositoryClass, repolizer) as? StorageAdapter<Body>
    protected val cacheAdapter: CacheAdapter? = AdapterUtil.getSafeAdapter(repolizer.cacheAdapters,
            bodyType, repositoryClass, repolizer) as? CacheAdapter

    override fun execute(): Body? {
        onStart()
        val result = onExecute(onDetermineExecutionType())
        onFinished()
        return result
    }

    override fun onDetermineExecutionType(): ExecutionType {
        return ExecutionType.USE_STORAGE
    }
}