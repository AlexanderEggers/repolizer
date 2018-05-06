package repolizer.repository

import repolizer.MainProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class RepositoryProcessorUtil {

    companion object {
        fun initMethodAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment,
                                  clazz: Class<out Annotation>, hashMap: HashMap<String, ArrayList<Element>>) {
            roundEnv.getElementsAnnotatedWith(clazz).forEach {
                if (it.kind != ElementKind.METHOD) {
                    mainProcessor.messager!!.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                            "be applied to a method.")
                    return
                }

                val typeElement = it.enclosingElement as TypeElement

                var currentList: ArrayList<Element>? = hashMap[typeElement.simpleName.toString()]
                if(currentList == null) {
                    currentList = ArrayList()
                }

                currentList.add(it)
                hashMap[typeElement.simpleName.toString()] = currentList
            }
        }

        fun initParamAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment,
                                 clazz: Class<out Annotation>, hashMap: HashMap<String, ArrayList<Element>>) {
            roundEnv.getElementsAnnotatedWith(clazz).forEach {
                if (it.kind != ElementKind.PARAMETER) {
                    mainProcessor.messager!!.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                            "be applied to a parameter.")
                    return
                }

                val typeElement = it.enclosingElement.enclosingElement as TypeElement

                var currentList: ArrayList<Element>? = hashMap[typeElement.simpleName.toString()]
                if(currentList == null) {
                    currentList = ArrayList()
                }

                currentList.add(it)
                hashMap[typeElement.simpleName.toString()] = currentList
            }
        }
    }
}
