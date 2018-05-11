package repolizer.repository.util

internal class Utils {

    companion object {
        private const val CACHE_URL_FORMAT_BASE = "CID"

        fun makeUrlId(url: String?): String {
            return if (url == null || url.isEmpty())
                "0"
            else
                CACHE_URL_FORMAT_BASE + url.hashCode().toString()
        }

        fun getGeneratedRepositoryName(repositoryClass: Class<*>): String {
            return "Generated_" + repositoryClass.simpleName + "_Repository"
        }

        fun getGeneratedDatabaseProviderName(databaseClass: Class<*>): String {
            return "Generated_" + databaseClass.simpleName + "_Database_Provider"
        }
    }
}