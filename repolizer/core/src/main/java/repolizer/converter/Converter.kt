package repolizer.converter

abstract class Converter<T> {
    abstract fun convertStringToData(repositoryClass: Class<*>, data: String): T?
    abstract fun convertDataToString(repositoryClass: Class<*>, data: Any): String?
}