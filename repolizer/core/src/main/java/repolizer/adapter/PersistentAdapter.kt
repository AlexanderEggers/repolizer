package repolizer.adapter

interface PersistentAdapter<T> {

    fun saveData(repositoryClass: Class<*>, url: String, data: T)

    fun saveCacheTime(repositoryClass: Class<*>, url: String)

    fun getCache(repositoryClass: Class<*>): T

    fun getCacheTime(repositoryClass: Class<*>): T
}