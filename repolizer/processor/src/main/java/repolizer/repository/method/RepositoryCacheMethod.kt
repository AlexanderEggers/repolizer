package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.CACHE
import repolizer.annotation.repository.util.CacheOperation
import repolizer.repository.RepositoryMapHolder
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic

class RepositoryCacheMethod {

    private val classDatabaseBuilder = ClassName.get("repolizer.repository.database", "DatabaseBuilder")
    private val classDatabaseLayer = ClassName.get("repolizer.repository.database", "DatabaseLayer")
    private val classCacheItem = ClassName.get("repolizer.database.cache", "CacheItem")

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val liveDataOfBoolean = ParameterizedTypeName.get(classLiveData, ClassName.get(java.lang.Boolean::class.java))

    fun build(messager: Messager, element: Element): List<MethodSpec> {
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
                            val networkCacheLayerClass = createCacheLayerForInsert(annotationMapKey,
                                    operation == CacheOperation.INSERT)

                            addStatement("$classDatabaseBuilder builder = new $classDatabaseBuilder()")
                            addStatement("builder.setDatabaseLayer($networkCacheLayerClass)")

                            //Determine the return value and if it's correct used by the user
                            val returnValue = ClassName.get(methodElement.returnType)
                            when {
                                returnValue == liveDataOfBoolean -> {
                                    returns(ClassName.get(methodElement.returnType))
                                    addStatement("return super.executeDB(builder)")
                                }
                                methodElement.returnType.kind == TypeKind.VOID -> addStatement("super.executeDB(builder)")
                                else -> messager.printMessage(Diagnostic.Kind.ERROR, "Methods that are using the " +
                                        "@CACHE annotation are only accepting LiveData<Boolean> or void as a " +
                                        "return type. Error for ${element.simpleName}.${methodElement.simpleName}")
                            }
                        }.build()
                    } ?: ArrayList())
        }
    }

    private fun createCacheLayerForInsert(annotationKey: String, isInsert: Boolean): TypeSpec {
        return TypeSpec.anonymousClassBuilder("").apply {
            addSuperinterface(classDatabaseLayer)

            addMethod(MethodSpec.methodBuilder("updateDB").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addStatement(createDaoCall(annotationKey, isInsert))
            }.build())
        }.build()
    }

    private fun createDaoCall(annotationKey: String, isInsert: Boolean): String {
        return ArrayList<String>().apply {
            RepositoryMapHolder.databaseBodyAnnotationMap[annotationKey]
                    ?.forEach { varElement ->
                        val varType = ClassName.get(varElement.asType())

                        if (varType == classCacheItem) {
                            add(varElement.simpleName.toString())
                        }
                    }
        }.joinToString(prefix = if (isInsert) "cacheDao.insert(" else "cacheDao.delete(", postfix = ")")
    }
}