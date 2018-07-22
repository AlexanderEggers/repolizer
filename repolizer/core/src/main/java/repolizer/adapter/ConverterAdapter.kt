package repolizer.adapter

abstract class ConverterAdapter {
    abstract fun <T> convertStringToData(repositoryClass: Class<*>, data: String, clazz: Class<T>): T?
    abstract fun convertDataToString(repositoryClass: Class<*>, data: Any): String?
}