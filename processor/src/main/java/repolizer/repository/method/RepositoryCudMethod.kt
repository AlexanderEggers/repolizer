package repolizer.repository.method

import com.squareup.javapoet.*
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.CUD
import repolizer.annotation.repository.util.CudType

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

    fun build(element: Element, entity: ClassName): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        RepositoryMapHolder.cudAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val entityBodyAsList = methodElement.getAnnotation(CUD::class.java).entityBodyAsList
            val classGenericTypeForMethod = if (entityBodyAsList) ParameterizedTypeName.get(classList, entity) else entity
            val cudType = methodElement.getAnnotation(CUD::class.java).cudType

            val getMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)
                    .returns(ClassName.get(methodElement.returnType))

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                getMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val url = methodElement.getAnnotation(CUD::class.java).url
            val requiresLogin = methodElement.getAnnotation(CUD::class.java).requiresLogin
            val showProgress = methodElement.getAnnotation(CUD::class.java).showProgress
            val classWithNetworkBuilder = ParameterizedTypeName.get(classNetworkBuilder, classGenericTypeForMethod)
            val requestType = getRequestType(cudType)

            getMethodBuilder.addStatement("$classNetworkBuilder builder = new $classWithNetworkBuilder()")
            getMethodBuilder.addStatement("builder.setRequestType($classRequestType.$requestType)")
            getMethodBuilder.addStatement("builder.setUrl(\"$url\")")
            getMethodBuilder.addStatement("builder.setRequiresLogin($requiresLogin)")
            getMethodBuilder.addStatement("builder.setShowProgress($showProgress)")

            RepositoryMapHolder.requestBodyAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                getMethodBuilder.addStatement("builder.setRaw(${it.simpleName})")
            }

            RepositoryMapHolder.headerAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                getMethodBuilder.addStatement("builder.addHeader(${it.simpleName})")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                getMethodBuilder.addStatement("builder.addQuery(${it.simpleName})")
            }

            val networkGetLayerClass = createNetworkPostLayerAnonymousClass(
                    classGenericTypeForMethod, cudType)
            getMethodBuilder.addStatement("builder.setNetworkLayer($networkGetLayerClass)")
            getMethodBuilder.addStatement("return super.executeCud(builder)")

            builderList.add(getMethodBuilder.build())
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
        return when(cudType) {
            CudType.POST -> "POST"
            CudType.PUT -> "PUT"
            CudType.DELETE -> "DELETE"
        }
    }

    private fun getCreateCallStatement(cudType: CudType): String {
        return when(cudType) {
            CudType.POST -> "controller.post(headerMap, url, queryMap, raw)"
            CudType.PUT -> "controller.put(headerMap, url, queryMap, raw)"
            CudType.DELETE -> "controller.delete(headerMap, url, queryMap, raw)"
        }
    }
}