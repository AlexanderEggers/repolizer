package repolizer.repository.request

import java.lang.reflect.Type

interface RequestProvider<C> {
    fun addRequest(url: String, call: C)
    fun removeRequest(url: String, call: C)
    fun cancelAllRequests()
    fun getRequestType(): Type
}