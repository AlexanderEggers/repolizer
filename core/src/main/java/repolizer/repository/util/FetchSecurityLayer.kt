package repolizer.repository.util

interface FetchSecurityLayer {
    fun allowFetch(): Boolean
    fun onFetchFinished()
}