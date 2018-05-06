package repolizer.util

import repolizer.MainProcessor
import javax.lang.model.element.Element

class ProcessorUtil {

    companion object {

        fun getPackageName(mainProcessor: MainProcessor, element: Element): String {
            return mainProcessor.elements!!.getPackageOf(element).qualifiedName.toString()
        }

        fun getGeneratedRepositoryName(repositoryName: String): String {
            return "Generated_" + repositoryName + "_Repository"
        }

        fun getGeneratedDatabaseName(databaseName: String): String {
            return "Generated_" + databaseName + "_Database"
        }

        fun getGeneratedDatabaseDao(databaseName: String, entityName: String): String {
            return "Generated_" + databaseName + "_" + entityName + "_Dao"
        }

        fun getGeneratedDatabaseProviderName(databaseClass: Class<*>): String {
            return "Generated_" + databaseClass.simpleName + "_Database_Provider"
        }
    }
}