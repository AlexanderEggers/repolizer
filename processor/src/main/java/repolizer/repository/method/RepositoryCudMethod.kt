package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.CUD
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.annotation.repository.util.CudType
import repolizer.repository.RepositoryMapHolder
import java.io.Serializable
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic

class RepositoryCudMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classNetworkLayer = ClassName.get("repolizer.repository.network", "NetworkCudLayer")
    private val classNetworkController = ClassName.get("repolizer.repository.api", "NetworkController")
    private val classRequestType = ClassName.get("repolizer.repository.request", "RequestType")
    private val classNetworkResponse = ClassName.get("repolizer.repository.response", "NetworkResponse")

    private val classSerializable = ClassName.get(Serializable::class.java)
    private val classMapWithString = ParameterizedTypeName.get(Map::class.java, String::class.java, String::class.java)
    private val classNetworkResponseWithString = ParameterizedTypeName.get(classNetworkResponse, ClassName.get(String::class.java))

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val liveDataOfString = ParameterizedTypeName.get(classLiveData, ClassName.get(String::class.java))

    fun build(messager: Messager, element: Element): List<MethodSpec> {
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

                            val url = methodElement.getAnnotation(CUD::class.java).url
                            addStatement("String url = \"$url\"")
                            addCode(buildUrl(annotationMapKey))

                            addCode(getBuilderCode(annotationMapKey, methodElement.getAnnotation(CUD::class.java)))

                            val returnValue = ClassName.get(methodElement.returnType)
                            if (returnValue == liveDataOfString) {
                                addStatement("return super.executeCud(builder)")
                            } else {
                                messager.printMessage(Diagnostic.Kind.ERROR, "Methods which are using the " +
                                        "@CUD annotation are only accepting LiveData<String> as a return type." +
                                        "Error for ${element.simpleName}.${methodElement.simpleName}")
                            }
                        }.build()
                    } ?: ArrayList())
        }
    }

    private fun buildUrl(annotationMapKey: String): String {
        return ArrayList<String>().apply {
            addAll(RepositoryMapHolder.urlParameterAnnotationMap[annotationMapKey]?.map {
                "url = url.replace(\":${it.simpleName}\", \"$it\");"
            } ?: ArrayList())
        }.joinToString(separator = "\n", postfix = "\n")
    }

    private fun getBuilderCode(annotationMapKey: String, annotation: CUD): String {
        return ArrayList<String>().apply {
            val classWithNetworkBuilder = ParameterizedTypeName.get(classNetworkBuilder, classSerializable)
            add("$classWithNetworkBuilder builder = new $classNetworkBuilder();")

            add("builder.setRequestType($classRequestType.${annotation.cudType.name});")
            add("builder.setRequiresLogin(${annotation.requiresLogin});")
            add("builder.setShowProgress(${annotation.showProgress});")
            add("builder.setUrl(url);")

            RepositoryMapHolder.requestBodyAnnotationMap[annotationMapKey]?.forEach {
                add("builder.setRaw(${it.simpleName});")
            }

            RepositoryMapHolder.headerAnnotationMap[annotationMapKey]?.forEach {
                add("builder.addHeader(" +
                        "\"${it.getAnnotation(Header::class.java).key}\", ${it.simpleName});")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]?.forEach {
                add("builder.addQuery(" +
                        "\"${it.getAnnotation(UrlQuery::class.java).key}\", ${it.simpleName});")
            }

            RepositoryMapHolder.progressParamsAnnotationMap[annotationMapKey]?.forEach {
                add("builder.setProgressParams(${it.simpleName});")
            }

            val networkGetLayerClass = createNetworkPostLayerAnonymousClass(annotation.cudType)
            add("builder.setNetworkLayer($networkGetLayerClass);")
        }.joinToString(separator = "\n", postfix = "\n")
    }

    private fun createNetworkPostLayerAnonymousClass(cudType: CudType): TypeSpec {
        return TypeSpec.anonymousClassBuilder("").apply {
            addSuperinterface(ParameterizedTypeName.get(classNetworkLayer, classSerializable))
            addMethod(MethodSpec.methodBuilder("createCall").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addParameter(classNetworkController, "controller")
                addParameter(classMapWithString, "headerMap")
                addParameter(ClassName.get(String::class.java), "url")
                addParameter(classMapWithString, "queryMap")
                addParameter(classSerializable, "raw")
                addStatement("return ${getCreateCallStatement(cudType)}")
                returns(ParameterizedTypeName.get(classLiveData, classNetworkResponseWithString))
            }.build())
        }.build()
    }

    private fun getCreateCallStatement(cudType: CudType): String {
        return when (cudType) {
            CudType.POST -> "controller.post(headerMap, url, queryMap, raw)"
            CudType.PUT -> "controller.put(headerMap, url, queryMap, raw)"
            CudType.DELETE -> "controller.delete(headerMap, url, queryMap, raw)"
        }
    }
}