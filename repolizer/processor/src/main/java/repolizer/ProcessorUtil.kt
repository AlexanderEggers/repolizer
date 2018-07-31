package repolizer

import javax.lang.model.element.Element

class ProcessorUtil {

    companion object {

        fun getPackageName(mainProcessor: MainProcessor, element: Element): String {
            return mainProcessor.elements.getPackageOf(element).qualifiedName.toString()
        }

        fun getGeneratedRepositoryName(repositoryName: String): String {
            return "Generated_$repositoryName"
        }
    }
}