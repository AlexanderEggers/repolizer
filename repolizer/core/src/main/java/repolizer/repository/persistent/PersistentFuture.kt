package repolizer.repository.persistent

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import repolizer.Repolizer
import repolizer.adapter.CacheAdapter
import repolizer.adapter.ConverterAdapter
import repolizer.adapter.StorageAdapter
import repolizer.adapter.util.AdapterUtil
import repolizer.converter.Converter
import repolizer.repository.future.Future
import repolizer.repository.network.ExecutionType

@Suppress("UNCHECKED_CAST")
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
    protected val wrapperType: TypeToken<*> = futureBuilder.typeToken
            ?: throw IllegalStateException("Wrapper type is null.")

    protected val storageAdapter: StorageAdapter<Body> = AdapterUtil.getAdapter(repolizer.storageAdapters,
            wrapperType.type, repositoryClass, repolizer) as StorageAdapter<Body>
    protected val cacheAdapter: CacheAdapter = AdapterUtil.getAdapter(repolizer.cacheAdapters,
            wrapperType.type, repositoryClass, repolizer) as CacheAdapter
    protected val converter: Converter<Body> = repolizer.converterClass
            .getConstructor(Gson::class.java).newInstance(repolizer.gson) as Converter<Body>

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