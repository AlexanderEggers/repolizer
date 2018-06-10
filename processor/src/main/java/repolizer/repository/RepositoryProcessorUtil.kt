package repolizer.repository

import repolizer.MainProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

class RepositoryProcessorUtil {

    companion object {
        fun initMethodAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment,
                                  clazz: Class<out Annotation>, hashMap: HashMap<String, ArrayList<ExecutableElement>>) {
            for (it in roundEnv.getElementsAnnotatedWith(clazz)) {
                val typeElement = it.enclosingElement as TypeElement

                if (it.kind != ElementKind.METHOD) {
                    mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                            "be applied to a method. Error for " +
                            "${typeElement.simpleName}.${it.simpleName}")
                    continue
                }

                val key = typeElement.simpleName.toString()
                val currentList: ArrayList<ExecutableElement> = hashMap[key] ?: ArrayList()
                currentList.add(it as ExecutableElement)
                hashMap[typeElement.simpleName.toString()] = currentList
            }
        }

        fun initParamAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment,
                                 clazz: Class<out Annotation>, hashMap: HashMap<String, ArrayList<VariableElement>>) {
            for (it in roundEnv.getElementsAnnotatedWith(clazz)) {
                val typeElement = it.enclosingElement.enclosingElement as TypeElement
                val methodElement = it.enclosingElement as ExecutableElement

                if (it.kind != ElementKind.PARAMETER) {
                    mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                            "be applied to a parameter. Error for " +
                            "${typeElement.simpleName}.${methodElement.simpleName}.${it.simpleName}")
                    continue
                }

                val key = "${typeElement.simpleName}.${methodElement.simpleName}"
                val currentList: ArrayList<VariableElement> = hashMap[key] ?: ArrayList()
                currentList.add(it as VariableElement)
                hashMap[key] = currentList
            }
        }
    }
}
