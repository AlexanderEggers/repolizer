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
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic

class RepositoryCacheMethod {

    private val classDatabaseBuilder = ClassName.get("repolizer.repository.database", "DatabaseBuilder")
    private val classDatabaseLayer = ClassName.get("repolizer.repository.database", "DatabaseLayer")
    private val classCacheItem = ClassName.get("repolizer.database.cache", "CacheItem")

    private val classString = ClassName.get(String::class.java)

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val liveDataOfBoolean = ParameterizedTypeName.get(classLiveData, ClassName.get(Boolean::class.java))

    fun build(messager: Messager, element: Element): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        RepositoryMapHolder.cacheAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val operation = methodElement.getAnnotation(CACHE::class.java).operation

            val cacheMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                cacheMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val networkGetLayerClass = if (operation == CacheOperation.INSERT || operation == CacheOperation.DELETE_SINGLE) {
                createCacheLayerForInsert(element, methodElement, operation == CacheOperation.INSERT)
            } else throw IllegalStateException("Cache operation unknown. Cache the value you inserted into your @Cache annotation.")

            cacheMethodBuilder.addStatement("$classDatabaseBuilder builder = new $classDatabaseBuilder()")
            cacheMethodBuilder.addStatement("builder.setDatabaseLayer($networkGetLayerClass)")

            val returnValue = ClassName.get(methodElement.returnType)
            when {
                returnValue == liveDataOfBoolean -> {
                    cacheMethodBuilder.returns(ClassName.get(methodElement.returnType))
                    cacheMethodBuilder.addStatement("return super.executeDB(builder)")
                }
                methodElement.returnType == TypeKind.VOID -> cacheMethodBuilder.addStatement("super.executeDB(builder)")
                else -> {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Methods which are using the " +
                            "@CACHE annotation are only accepting LiveData<Boolean> or Void as a " +
                            "return type.")
                    return emptyList()
                }
            }

            builderList.add(cacheMethodBuilder.build())
        }

        return builderList
    }

    private fun createCacheLayerForInsert(element: Element, methodElement: ExecutableElement, isInsert: Boolean): TypeSpec {
        val bodyCacheItemParamList = ArrayList<String>()
        val bodyStringParamList = ArrayList<String>()
        val doaCallStart = if (isInsert) "cacheDao.insert(" else "cacheDao.delete("

        RepositoryMapHolder.databaseBodyAnnotationMap["${element.simpleName}.${methodElement.simpleName}"]?.forEach { varElement ->
            val varType = ClassName.get(varElement.asType())

            if (varType == classCacheItem) {
                bodyCacheItemParamList.add(varElement.simpleName.toString())
            } else if (varType == classString) {
                bodyStringParamList.add(varElement.simpleName.toString())
            }
        }

        val builder = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(classDatabaseLayer)

        val methodBuilder = MethodSpec.methodBuilder("updateDB")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)

        if (!bodyCacheItemParamList.isEmpty()) {
            methodBuilder.addStatement(createDaoCall(doaCallStart, bodyCacheItemParamList))
        } else if (!bodyStringParamList.isEmpty()) {
            if (!isInsert) {
                methodBuilder.addStatement(createDaoCall(doaCallStart, bodyStringParamList))
            }
        }

        return builder.addMethod(methodBuilder.build()).build()
    }

    private fun createDaoCall(doaCallStart: String, list: ArrayList<String>): String {
        var daoCall = doaCallStart

        val iterator = list.iterator()
        while (iterator.hasNext()) {
            daoCall += iterator.next()
            daoCall += if (iterator.hasNext()) ", " else ""
        }

        return "$daoCall)"
    }
}