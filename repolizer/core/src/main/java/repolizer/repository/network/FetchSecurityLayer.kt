package repolizer.repository.network

interface FetchSecurityLayer {
    fun allowFetch(): Boolean
    fun onFetchFinished()
}