package repolizer.adapter

interface ImageAdapter<T> {

    fun execute(url: String): T
}