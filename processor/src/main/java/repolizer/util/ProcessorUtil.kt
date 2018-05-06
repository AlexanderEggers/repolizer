package repolizer.util

class ProcessorUtil {

    companion object {

        fun getGeneratedRepositoryName(repositoryClass: Class<*>): String {
            return "Generated_" + repositoryClass.simpleName + "_Repository"
        }

        fun getGeneratedDatabaseName(databaseClass: Class<*>): String {
            return "Generated_" + databaseClass.simpleName + "_Database"
        }

        fun getGeneratedDatabaseProviderName(databaseClass: Class<*>): String {
            return "Generated_" + databaseClass.simpleName + "_Database_Provider"
        }
    }
}