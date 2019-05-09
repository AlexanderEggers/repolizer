package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import repolizer.annotation.repository.DATA
import repolizer.annotation.repository.util.DataOperation
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryDataMethod {

    private val classDatabaseRequest = ClassName.get("repolizer.repository.persistent", "PersistentFutureRequestBuilder")
    private val classDataOperation = ClassName.get("repolizer.annotation.repository.util", "DataOperation")

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
                            val annotation = methodElement.getAnnotation(DATA::class.java)

                            val operationStatement = annotation.operationStatement
                            addStatement("String operationStatement = \"$operationStatement\"")
                            if (operationStatement.isNotEmpty()) addCode(buildStatement(
                                    "operationStatement", annotationMapKey))

                            val returnStatement = annotation.operationStatement
                            addStatement("String returnStatement = \"$returnStatement\"")
                            if (returnStatement.isNotEmpty()) addCode(buildStatement(
                                    "returnStatement", annotationMapKey))

                            val classWithTypeToken = ParameterizedTypeName.get(classTypeToken,
                                    ClassName.get(methodElement.returnType))
                            addStatement("$classTypeToken returnType = new $classWithTypeToken() {}")

                            addCode("\n")

                            addStatement("$classDatabaseRequest request = new $classDatabaseRequest()")
                            addStatement("request.setRepositoryClass(${ClassName.get(element.asType())}.class)")
                            addStatement("request.setTypeToken(returnType)")

                            val operation = methodElement.getAnnotation(DATA::class.java).operation
                            addStatement("request.setDataOperation($classDataOperation.$operation)")
                            addStatement(getOperationBuilderMethod(operation))
                            addStatement("request.setReturnStatement(returnStatement)")
                            addStatement("request.setOverrideEmptyReturnStatement(${annotation.overrideEmptyReturnStatement})")

                            createDataItemBuilderMethods(annotationMapKey).forEach {
                                addStatement(it)
                            }

                            addStatement("return super.executeData(request, returnType.getType())")
                        }.build()
                    } ?: ArrayList())
        }
    }

    private fun getOperationBuilderMethod(operation: DataOperation): String {
        return when (operation) {
            DataOperation.INSERT -> "request.setInsertStatement(operationStatement)"
            DataOperation.UPDATE -> "request.setUpdateStatement(operationStatement)"
            DataOperation.DELETE -> "request.setDeleteStatement(operationStatement)"
        }
    }

    private fun createDataItemBuilderMethods(annotationKey: String): ArrayList<String> {
        return ArrayList<String>().apply {
            RepositoryMapHolder.dataBodyAnnotationMap[annotationKey]?.forEach { varElement ->
                add("request.setDataObject(${varElement.simpleName})")
            }
        }
    }

    private fun buildStatement(parameter: String, annotationMapKey: String): String {
        return (RepositoryMapHolder.statementParameterAnnotationMap[annotationMapKey]?.map {
            "$parameter = $parameter.replace(\":${it.simpleName}\", ${it.simpleName} + \"\");"
        } ?: ArrayList()).joinToString(separator = "\n", postfix = "\n\n")
    }
}