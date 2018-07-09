package repolizer.repository.network

import repolizer.Repolizer
import repolizer.persistent.CacheItem
import repolizer.persistent.CacheState
import repolizer.repository.response.NetworkResponse
import repolizer.repository.response.ResponseService

class NetworkGetFuture<Body>
constructor(repolizer: Repolizer, futureBuilder: NetworkFutureBuilder<Body>) : NetworkFuture<Body>(repolizer, futureBuilder) {

    private val responseService: ResponseService? = repolizer.responseService
    private val deleteIfCacheIsTooOld: Boolean = futureBuilder.isDeletingCacheIfTooOld
    private var allowFetch: Boolean = false

    private lateinit var fetchSecurityLayer: FetchSecurityLayer
    private lateinit var cacheState: CacheState

    override fun onDetermineExecutionType(): ExecutionType {
        val cacheData = storageAdapter.get(String::class.java, fullUrl)
        val cacheState = cacheAdapter.get(String::class.java, fullUrl)
        val needsFetch = cacheState == CacheState.NEEDS_SOFT_REFRESH ||
                cacheState == CacheState.NEEDS_HARD_REFRESH || cacheState == CacheState.NO_CACHE

        return if ((cacheData == null || needsFetch) && allowFetch) {
            if (fetchSecurityLayer.allowFetch()) {
                ExecutionType.USE_NETWORK
            } else {
                ExecutionType.USE_CACHE
            }
        } else {
            ExecutionType.USE_CACHE
        }
    }

    override fun onExecute(executionType: ExecutionType): Body? {
        return when (executionType) {
            ExecutionType.USE_NETWORK -> fetchFromNetwork()
            ExecutionType.USE_CACHE -> fetchCacheData()
        }
    }

    private fun fetchFromNetwork(): Body? {
        val response: NetworkResponse<String> = networkAdapter.execute(this)

        return if (response.isSuccessful() && response.body != null) {
            responseService?.handleSuccess(requestType, response)
            storageAdapter.save(String::class.java, fullUrl, response.body)
            cacheAdapter.save(String::class.java, CacheItem(fullUrl, System.currentTimeMillis()))
            storageAdapter.get(String::class.java, fullUrl)
        } else {
            responseService?.handleRequestError(requestType, response)
            if (deleteIfCacheIsTooOld && cacheState == CacheState.NEEDS_HARD_REFRESH) {
                storageAdapter.delete(String::class.java, fullUrl, true)
            }
            null
        }
    }

    private fun fetchCacheData(): Body? {
        return storageAdapter.get(String::class.java, fullUrl)
    }


    override fun onFinished() {
        super.onFinished()
        fetchSecurityLayer.onFetchFinished()
    }
}
