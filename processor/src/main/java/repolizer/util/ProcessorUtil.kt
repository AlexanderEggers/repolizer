package repolizer.util

import repolizer.MainProcessor
import javax.lang.model.element.Element

class ProcessorUtil {

    companion object {

        fun getPackageName(mainProcessor: MainProcessor, element: Element): String {
            return mainProcessor.elements.getPackageOf(element).qualifiedName.toString()
        }

        fun getGeneratedRepositoryName(repositoryName: String): String {
            return "Generated_$repositoryName"
        }

        fun getGeneratedDatabaseName(databaseName: String): String {
            return "Generated_$databaseName"
        }

        fun getGeneratedDatabaseDaoName(databaseName: String, entityName: String): String {
            return "Generated_${databaseName}_${entityName}_Dao"
        }

        fun getGeneratedDatabaseProviderName(databaseName: String): String {
            return "Generated_${databaseName}_Provider"
        }
    }
}