package repolizer.database

import com.squareup.javapoet.ClassName
import repolizer.MainProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.tools.Diagnostic

class DatabaseProcessorUtil {

    companion object {
        fun initClassAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment,
                                  clazz: Class<out Annotation>, hashMap: HashMap<String, ArrayList<Element>>) {
            roundEnv.getElementsAnnotatedWith(clazz).forEach {
                if (it.kind != ElementKind.METHOD) {
                    mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                            "be applied to a class.")
                    return
                }

                var currentList: ArrayList<Element>? = hashMap[it.simpleName.toString()]
                if(currentList == null) {
                    currentList = ArrayList()
                }

                currentList.add(it)
                hashMap[it.simpleName.toString()] = currentList
            }
        }

        fun addDaoToDatabaseMap(databaseName: String, daoName: ClassName) {
            val hashMap = DatabaseMapHolder.daoMap

            var currentList: ArrayList<ClassName>? = hashMap[databaseName]
            if (currentList == null) {
                currentList = ArrayList()
            }
            currentList.add(daoName)
            hashMap[databaseName] = currentList
        }
    }
}
