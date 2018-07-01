package repolizer.adapter

interface StorageAdapter<T> {

    fun save(repositoryClass: Class<*>, url: String, data: T)

    fun get(repositoryClass: Class<*>, url: String): T

    fun delete(repositoryClass: Class<*>, url: String)
}