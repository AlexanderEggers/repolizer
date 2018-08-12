package repolizer.repository

import repolizer.MainProcessor
import repolizer.annotation.repository.parameter.UrlQuery
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
            roundEnv.getElementsAnnotatedWith(clazz).forEach {
                val typeElement = it.enclosingElement as TypeElement

                if (it.kind != ElementKind.METHOD) {
                    mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR,
                            "@${clazz.simpleName} can only be applied to a method. Error for " +
                                    "${typeElement.simpleName}.${it.simpleName}")
                }

                val key = typeElement.simpleName.toString()
                val currentList: ArrayList<ExecutableElement> = hashMap[key] ?: ArrayList()
                currentList.add(it as ExecutableElement)
                hashMap[typeElement.simpleName.toString()] = currentList
            }
        }

        fun initParamAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment,
                                 clazz: Class<out Annotation>, hashMap: HashMap<String, ArrayList<VariableElement>>) {
            roundEnv.getElementsAnnotatedWith(clazz).forEach {
                val typeElement = it.enclosingElement.enclosingElement as TypeElement
                val methodElement = it.enclosingElement as ExecutableElement

                if (it.kind != ElementKind.PARAMETER) {
                    mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR,
                            "@${clazz.simpleName} can only be applied to a parameter. Error for " +
                                    "${typeElement.simpleName}.${methodElement.simpleName}.${it.simpleName}")
                }

                val key = "${typeElement.simpleName}.${methodElement.simpleName}"
                val currentList: ArrayList<VariableElement> = hashMap[key] ?: ArrayList()
                currentList.add(it as VariableElement)
                hashMap[key] = currentList
            }
        }

        fun buildUrl(annotationMapKey: String): String {
            return ArrayList<String>().apply {
                addAll(RepositoryMapHolder.urlParameterAnnotationMap[annotationMapKey]?.map {
                    "url = url.replace(\":${it.simpleName}\", ${it.simpleName} + \"\");"
                } ?: ArrayList())

                val queries = RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]
                if (queries?.isNotEmpty() == true) add(getFullUrlQueryPart(queries))
            }.joinToString(separator = "\n", postfix = "\n\n")
        }

        private fun getFullUrlQueryPart(queries: ArrayList<VariableElement>): String {
            return (queries.map { urlQuery ->
                "url += " + "\"${urlQuery.getAnnotation(UrlQuery::class.java).key}=\" + ${urlQuery.simpleName};"
            }).joinToString(prefix = "url += \"?\";", separator = "\nurl += \"&\";\n")
        }

        fun buildSql(annotationMapKey: String, sqlParamName: String, baseSql: String): String {
            return ArrayList<String>().apply {
                RepositoryMapHolder.sqlParameterAnnotationMap[annotationMapKey]?.forEach {
                    if (baseSql.contains(":${it.simpleName}")) {
                        add("$sqlParamName = $sqlParamName.replace(\":${it.simpleName}\", ${it.simpleName} + \"\");")
                    }
                }
            }.joinToString(separator = "\n", postfix = "\n\n")
        }
    }
}
