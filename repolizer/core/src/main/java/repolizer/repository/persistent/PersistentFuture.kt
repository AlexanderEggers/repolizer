package repolizer.repository.persistent

import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.adapter.CacheAdapter
import repolizer.adapter.StorageAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.repository.future.Future
import repolizer.repository.network.ExecutionType

abstract class PersistentFuture<Body>
constructor(protected val repolizer: Repolizer, futureBuilder: PersistentFutureBuilder) : Future<Body>() {

    val fullUrl: String by lazy {
        repolizer.baseUrl?.let { baseUrl ->
            if (baseUrl.substring(baseUrl.length) != "/") {
                "$baseUrl/${futureBuilder.url}"
            } else {
                "$baseUrl${futureBuilder.url}"
            }
        } ?: futureBuilder.url
    }

    protected val repositoryClass: Class<*> = futureBuilder.repositoryClass
            ?: throw IllegalStateException("Repository class type is null.")
    protected val bodyType: TypeToken<*> = futureBuilder.typeToken
            ?: throw IllegalStateException("Body type is null.")

    protected val storageAdapter: StorageAdapter<Body> = AdapterUtil.getAdapter(repolizer.storageAdapters,
            bodyType.type, repositoryClass, repolizer) as StorageAdapter<Body>
    protected val cacheAdapter: CacheAdapter = AdapterUtil.getAdapter(repolizer.cacheAdapters,
            bodyType.type, repositoryClass, repolizer) as CacheAdapter

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