package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import repolizer.annotation.repository.CACHE
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryCacheMethod {

    private val classPersistentBuilder = ClassName.get("repolizer.repository.database", "PersistentFutureBuilder")
    private val classCacheItem = ClassName.get("repolizer.persistent", "CacheItem")

    fun build(element: Element): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.cacheAnnotationMap[element.simpleName.toString()]
                    ?.map { methodElement ->
                        MethodSpec.methodBuilder(methodElement.simpleName.toString()).apply {
                            addModifiers(Modifier.PUBLIC)
                            addAnnotation(Override::class.java)

                            //Copy all interface parameter to the method implementation
                            methodElement.parameters.forEach { varElement ->
                                val varType = ClassName.get(varElement.asType())
                                addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
                            }

                            val annotationMapKey = "${element.simpleName}.${methodElement.simpleName}"
                            val operation = methodElement.getAnnotation(CACHE::class.java).operation

                            addStatement("$classPersistentBuilder builder = new $classPersistentBuilder()")
                            addStatement("builder.setCacheOperation($operation)")
                            addStatement("builder.setRepositoryClass(${ClassName.get(element.asType())}.class)")

                            createCacheItemBuilderMethods(annotationMapKey).forEach {
                                addStatement(it)
                            }

                            addStatement("return super.executeStorage(builder)")
                        }.build()
                    } ?: ArrayList())
        }
    }

    private fun createCacheItemBuilderMethods(annotationKey: String): ArrayList<String> {
        return ArrayList<String>().apply {
            RepositoryMapHolder.storageBodyAnnotationMap[annotationKey]?.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())

                if (varType == classCacheItem) {
                    add("builder.setCacheItem(${varElement.simpleName})")
                }
            }
        }
    }
}