package repolizer.repository.method

import com.squareup.javapoet.*
import repolizer.annotation.repository.CUD
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.annotation.repository.util.CudType
import repolizer.repository.RepositoryMapHolder
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic

class RepositoryCudMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classNetworkLayer = ClassName.get("repolizer.repository.network", "NetworkCudLayer")
    private val classNetworkController = ClassName.get("repolizer.repository.api", "NetworkController")
    private val classRequestType = ClassName.get("repolizer.repository.util", "RequestType")
    private val classNetworkResponse = ClassName.get("repolizer.repository.response", "NetworkResponse")

    private val classList = ClassName.get(List::class.java)
    private val classMapWithString = ParameterizedTypeName.get(Map::class.java, String::class.java, String::class.java)
    private val classNetworkResponseWithString = ParameterizedTypeName.get(classNetworkResponse, ClassName.get(String::class.java))

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val liveDataOfString = ParameterizedTypeName.get(classLiveData, ClassName.get(String::class.java))

    fun build(messager: Messager, element: Element, entity: ClassName): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        val list = RepositoryMapHolder.cudAnnotationMap[element.simpleName.toString()] ?: ArrayList()
        for (methodElement in list) {
            val entityBodyAsList = methodElement.getAnnotation(CUD::class.java).entityBodyAsList
            val classGenericTypeForMethod = if (entityBodyAsList) ParameterizedTypeName.get(classList, entity) else entity
            val cudType = methodElement.getAnnotation(CUD::class.java).cudType

            val cudMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)
                    .returns(ClassName.get(methodElement.returnType))

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                cudMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val url = methodElement.getAnnotation(CUD::class.java).url
            val requiresLogin = methodElement.getAnnotation(CUD::class.java).requiresLogin
            val showProgress = methodElement.getAnnotation(CUD::class.java).showProgress
            val classWithNetworkBuilder = ParameterizedTypeName.get(classNetworkBuilder, classGenericTypeForMethod)
            val requestType = getRequestType(cudType)

            cudMethodBuilder.addStatement("String url = \"$url\"")
            RepositoryMapHolder.urlParameterAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                cudMethodBuilder.addStatement("url = url.replace(\":${it.simpleName}\", \"$it\")")
            }
            cudMethodBuilder.addCode("\n")

            cudMethodBuilder.addStatement("$classNetworkBuilder builder = new $classWithNetworkBuilder()")
            cudMethodBuilder.addStatement("builder.setRequestType($classRequestType.$requestType)")
            cudMethodBuilder.addStatement("builder.setUrl(url)")
            cudMethodBuilder.addStatement("builder.setRequiresLogin($requiresLogin)")
            cudMethodBuilder.addStatement("builder.setShowProgress($showProgress)")

            RepositoryMapHolder.requestBodyAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                cudMethodBuilder.addStatement("builder.setRaw(${it.simpleName})")
            }

            RepositoryMapHolder.headerAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                cudMethodBuilder.addStatement("builder.addHeader(" +
                        "\"${it.getAnnotation(Header::class.java).key}\", ${it.simpleName})")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                cudMethodBuilder.addStatement("builder.addQuery(" +
                        "\"${it.getAnnotation(UrlQuery::class.java).key}\", ${it.simpleName})")
            }

            val networkGetLayerClass = createNetworkPostLayerAnonymousClass(
                    classGenericTypeForMethod, cudType)
            cudMethodBuilder.addStatement("builder.setNetworkLayer($networkGetLayerClass)")

            val returnValue = ClassName.get(methodElement.returnType)
            if (returnValue == liveDataOfString) {
                cudMethodBuilder.addStatement("return super.executeCud(builder)")
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "Methods which are using the " +
                        "@CUD annotation are only accepting LiveData<String> as a return type." +
                        "Error for class.method: ${element.simpleName}.${methodElement.simpleName}")
                continue
            }

            builderList.add(cudMethodBuilder.build())
        }

        return builderList
    }

    private fun createNetworkPostLayerAnonymousClass(classGenericTypeForMethod: TypeName, cudType: CudType): TypeSpec {
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(classNetworkLayer, classGenericTypeForMethod))
                .addMethod(MethodSpec.methodBuilder("createCall")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(classNetworkController, "controller")
                        .addParameter(classMapWithString, "headerMap")
                        .addParameter(ClassName.get(String::class.java), "url")
                        .addParameter(classMapWithString, "queryMap")
                        .addParameter(classGenericTypeForMethod, "raw")
                        .addStatement("return ${getCreateCallStatement(cudType)}")
                        .returns(ParameterizedTypeName.get(classLiveData, classNetworkResponseWithString))
                        .build())
                .build()
    }

    private fun getRequestType(cudType: CudType): String {
        return when (cudType) {
            CudType.POST -> "POST"
            CudType.PUT -> "PUT"
            CudType.DELETE -> "DELETE"
        }
    }

    private fun getCreateCallStatement(cudType: CudType): String {
        return when (cudType) {
            CudType.POST -> "controller.post(headerMap, url, queryMap, raw)"
            CudType.PUT -> "controller.put(headerMap, url, queryMap, raw)"
            CudType.DELETE -> "controller.delete(headerMap, url, queryMap, raw)"
        }
    }
}