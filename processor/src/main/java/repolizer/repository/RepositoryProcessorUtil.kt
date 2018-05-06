package repolizer.repository

import repolizer.MainProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import javax.tools.Diagnostic

class RepositoryProcessorUtil {

    companion object {
        fun initMethodAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment,
                                  clazz: Class<out Annotation>, hashMap: HashMap<String, ArrayList<ExecutableElement>>) {
            roundEnv.getElementsAnnotatedWith(clazz).forEach {
                if (it.kind != ElementKind.METHOD) {
                    mainProcessor.messager!!.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                            "be applied to a method.")
                    return
                }

                val typeElement = it.enclosingElement as TypeElement

                var currentList: ArrayList<ExecutableElement>? = hashMap[typeElement.simpleName.toString()]
                if(currentList == null) {
                    currentList = ArrayList()
                }

                currentList.add(it as ExecutableElement)
                hashMap[typeElement.simpleName.toString()] = currentList
            }
        }

        fun initParamAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment,
                                 clazz: Class<out Annotation>, hashMap: HashMap<String, ArrayList<VariableElement>>) {
            roundEnv.getElementsAnnotatedWith(clazz).forEach {
                if (it.kind != ElementKind.PARAMETER) {
                    mainProcessor.messager!!.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                            "be applied to a parameter.")
                    return
                }

                val typeElement = it.enclosingElement.enclosingElement as TypeElement

                var currentList: ArrayList<VariableElement>? = hashMap[typeElement.simpleName.toString()]
                if(currentList == null) {
                    currentList = ArrayList()
                }

                currentList.add(it as VariableElement)
                hashMap[typeElement.simpleName.toString()] = currentList
            }
        }
    }
}
