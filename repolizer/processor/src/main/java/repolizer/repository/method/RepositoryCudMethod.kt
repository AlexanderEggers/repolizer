package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import repolizer.annotation.repository.CUD
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.repository.RepositoryMapHolder
import repolizer.repository.RepositoryProcessorUtil.Companion.buildUrl
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryCudMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkFutureBuilder")
    private val classRequestType = ClassName.get("repolizer.repository.request", "RequestType")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")

    fun build(element: Element): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.cudAnnotationMap[element.simpleName.toString()]
                    ?.map { methodElement ->
                        MethodSpec.methodBuilder(methodElement.simpleName.toString()).apply {
                            addModifiers(Modifier.PUBLIC)
                            addAnnotation(Override::class.java)
                            returns(ClassName.get(methodElement.returnType))

                            //Copy all interface parameter to the method implementation
                            methodElement.parameters.forEach { varElement ->
                                val varType = ClassName.get(varElement.asType())
                                addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
                            }

                            val annotationMapKey = "${element.simpleName}.${methodElement.simpleName}"

                            //Generates the code which used to retrieve the url from the annotation
                            //and dynamic parameter with method parameter (like the url part
                            //':myVar' could be the value '0'
                            val url = methodElement.getAnnotation(CUD::class.java).url
                            addStatement("String url = \"$url\"")
                            addCode(buildUrl(annotationMapKey))

                            val classWithTypeToken = ParameterizedTypeName.get(classTypeToken,
                                    ClassName.get(methodElement.returnType))
                            addStatement("$classTypeToken returnType = new $classWithTypeToken() {}")

                            addCode("\n")

                            //Generates the code which will be used for the NetworkBuilder to
                            //initialise it's values
                            addCode(getBuilderCode(annotationMapKey, element,
                                    methodElement.getAnnotation(CUD::class.java)))

                            addStatement("return super.executeCud(builder, returnType.getType())")
                        }.build()
                    } ?: ArrayList())
        }
    }

    private fun getBuilderCode(annotationMapKey: String, element: Element, annotation: CUD): String {
        return ArrayList<String>().apply {
            add("$classNetworkBuilder builder = new $classNetworkBuilder();")

            add("builder.setRepositoryClass(${ClassName.get(element.asType())}.class);")
            add("builder.setRequestType($classRequestType.${annotation.cudType.name});")
            add("builder.setTypeToken(returnType);")
            add("builder.setRequiresLogin(${annotation.requiresLogin});")
            add("builder.setUrl(url);")
            add("builder.setSaveData(false);")

            RepositoryMapHolder.requestBodyAnnotationMap[annotationMapKey]?.forEach {
                add("builder.addRaw(${it.simpleName});")
            }

            RepositoryMapHolder.multipartBodyAnnotationMap[annotationMapKey]?.forEach {
                add("builder.addMultipartBody(${it.simpleName});")
            }

            RepositoryMapHolder.headerAnnotationMap[annotationMapKey]?.forEach {
                add("builder.addHeader(" +
                        "\"${it.getAnnotation(Header::class.java).key}\", ${it.simpleName});")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]?.forEach {
                add("builder.addQuery(" +
                        "\"${it.getAnnotation(UrlQuery::class.java).key}\", ${it.simpleName});")
            }
        }.joinToString(separator = "\n", postfix = "\n")
    }
}