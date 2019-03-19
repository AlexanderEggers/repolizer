package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import repolizer.annotation.repository.STORAGE
import repolizer.annotation.repository.util.StorageOperation
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryStorageMethod {

    private val classDatabaseRequest = ClassName.get("repolizer.repository.persistent", "NetworkFutureRequest")
    private val classStorageOperation = ClassName.get("repolizer.annotation.repository.util", "StorageOperation")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")

    fun build(element: Element): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.dbAnnotationMap[element.simpleName.toString()]
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

                            val sql = methodElement.getAnnotation(STORAGE::class.java).sql
                            addStatement("String sql = \"$sql\"")
                            if (sql.isNotEmpty()) addCode(buildSql(annotationMapKey))

                            val classWithTypeToken = ParameterizedTypeName.get(classTypeToken,
                                    ClassName.get(methodElement.returnType))
                            addStatement("$classTypeToken returnType = new $classWithTypeToken() {}")

                            addCode("\n")

                            addStatement("$classDatabaseRequest request = new $classDatabaseRequest()")
                            addStatement("request.setRepositoryClass(${ClassName.get(element.asType())}.class)")
                            addStatement("request.setTypeToken(returnType)")

                            val operation = methodElement.getAnnotation(STORAGE::class.java).operation
                            addStatement("request.setStorageOperation($classStorageOperation.$operation)")
                            addStatement(getStorageSql(operation))

                            createStorageItemBuilderMethods(annotationMapKey).forEach {
                                addStatement(it)
                            }

                            addStatement("return super.executeStorage(request, returnType.getType())")
                        }.build()
                    } ?: ArrayList())
        }
    }

    private fun getStorageSql(operation: StorageOperation): String {
        return when (operation) {
            StorageOperation.INSERT -> "request.setInsertSql(sql)"
            StorageOperation.UPDATE -> "request.setUpdateSql(sql)"
            StorageOperation.DELETE -> "request.setDeleteSql(sql)"
        }
    }

    private fun createStorageItemBuilderMethods(annotationKey: String): ArrayList<String> {
        return ArrayList<String>().apply {
            RepositoryMapHolder.storageBodyAnnotationMap[annotationKey]?.forEach { varElement ->
                add("request.setStorageItem(${varElement.simpleName})")
            }
        }
    }

    private fun buildSql(annotationMapKey: String): String {
        return (RepositoryMapHolder.sqlParameterAnnotationMap[annotationMapKey]?.map {
            "sql = sql.replace(\":${it.simpleName}\", ${it.simpleName} + \"\");"
        } ?: ArrayList()).joinToString(separator = "\n", postfix = "\n\n")
    }
}