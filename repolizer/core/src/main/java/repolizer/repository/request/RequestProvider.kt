package repolizer.repository.request

interface RequestProvider<C> {
    fun addRequest(url: String, call: C)
    fun removeRequest(url: String, call: C)
    fun cancelAllRequests()
}