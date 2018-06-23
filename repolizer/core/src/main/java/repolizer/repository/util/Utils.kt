package repolizer.repository.util

class Utils {

    companion object {
        private const val CACHE_URL_FORMAT_BASE = "CID"

        fun makeUrlId(url: String): String {
            return CACHE_URL_FORMAT_BASE + url.hashCode().toString()
        }

        fun getGeneratedRepositoryName(repositoryClass: Class<*>): String {
            return "Generated_${repositoryClass.simpleName}"
        }

        fun getGeneratedDatabaseProviderName(databaseClass: Class<*>): String {
            return "Generated_${databaseClass.simpleName}_Provider"
        }

        fun prepareUrl(url: String): String {
            return url.split("?")[0]
        }
    }
}