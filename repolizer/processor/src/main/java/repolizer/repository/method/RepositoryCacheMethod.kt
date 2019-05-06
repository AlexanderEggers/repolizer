package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import repolizer.annotation.repository.CACHE
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryCacheMethod {

    private val classPersistentRequest = ClassName.get("repolizer.repository.persistent", "PersistentFutureRequestBuilder")
    private val classCacheItem = ClassName.get("repolizer.persistent", "CacheItem")
    private val classCacheOperation = ClassName.get("repolizer.annotation.repository.util", "CacheOperation")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")

    fun build(element: Element): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.cacheAnnotationMap[element.simpleName.toString()]
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
                            val operation = methodElement.getAnnotation(CACHE::class.java).operation

                            val classWithTypeToken = ParameterizedTypeName.get(classTypeToken,
                                    ClassName.get(methodElement.returnType))
                            addStatement("$classTypeToken returnType = new $classWithTypeToken() {}")

                            addCode("\n")

                            addStatement("$classPersistentRequest request = new $classPersistentRequest()")
                            addStatement("request.setCacheOperation($classCacheOperation.$operation)")
                            addStatement("request.setRepositoryClass(${ClassName.get(element.asType())}.class)")
                            addStatement("request.setTypeToken(new $classWithTypeToken() {})")

                            createCacheItemBuilderMethods(annotationMapKey).forEach {
                                addStatement(it)
                            }

                            addStatement("return super.executeCache(request, returnType.getType())")
                        }.build()
                    } ?: ArrayList())
        }
    }

    private fun createCacheItemBuilderMethods(annotationKey: String): ArrayList<String> {
        return ArrayList<String>().apply {
            RepositoryMapHolder.cacheBodyAnnotationMap[annotationKey]?.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())

                if (varType == classCacheItem) {
                    add("request.setCacheItem(${varElement.simpleName})")
                }
            }
        }
    }
}