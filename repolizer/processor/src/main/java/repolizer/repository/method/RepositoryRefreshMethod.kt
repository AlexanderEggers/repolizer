package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import repolizer.annotation.repository.REFRESH
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class RepositoryRefreshMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkFutureBuilder")
    private val classRequestType = ClassName.get("repolizer.repository.request", "RequestType")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")

    fun build(element: Element): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.refreshAnnotationMap[element.simpleName.toString()]?.map { methodElement ->
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
                    val url = methodElement.getAnnotation(REFRESH::class.java).url
                    addStatement("String url = \"$url\"")
                    addCode(buildUrl(annotationMapKey))

                    val sql = methodElement.getAnnotation(REFRESH::class.java).insertSql
                    addStatement("String insertSql = \"$sql\"")
                    if (sql.isNotEmpty()) addCode(buildSql(annotationMapKey))

                    addCode("\n")

                    val classWithTypeToken = ParameterizedTypeName.get(classTypeToken,
                            ClassName.get(methodElement.returnType))
                    addStatement("$classTypeToken returnType = new $classWithTypeToken() {}")

                    addCode("\n")

                    //Generates the code which will be used for the NetworkBuilder to
                    //initialise it's values
                    addCode(getBuilderCode(annotationMapKey, element, methodElement))

                    addStatement("return super.executeRefresh(builder, returnType)")
                }.build()
            } ?: ArrayList())
        }
    }

    private fun buildUrl(annotationMapKey: String): String {
        return ArrayList<String>().apply {
            addAll(RepositoryMapHolder.urlParameterAnnotationMap[annotationMapKey]?.map {
                "url = url.replace(\":${it.simpleName}\", ${it.simpleName});"
            } ?: ArrayList())

            val queries = RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]
            if (queries?.isNotEmpty() == true) add(getFullUrlQueryPart(queries))
        }.joinToString(separator = "\n", postfix = "\n\n")
    }

    private fun getFullUrlQueryPart(queries: ArrayList<VariableElement>): String {
        return (queries.map { urlQuery ->
            "url += " + "\"${urlQuery.getAnnotation(UrlQuery::class.java).key}=\" + ${urlQuery.simpleName};"
        }).joinToString(prefix = "url += \"?\";", separator = "\nurl += \"&\";\n")
    }

    private fun buildSql(annotationMapKey: String): String {
        return (RepositoryMapHolder.sqlParameterAnnotationMap[annotationMapKey]?.map {
            "insertSql = insertSql.replace(\":${it.simpleName}\", \"$it\");"
        } ?: ArrayList()).joinToString(separator = "\n", postfix = "\n\n")
    }

    private fun getBuilderCode(annotationMapKey: String, classElement: Element,
                               methodElement: ExecutableElement): String {
        return ArrayList<String>().apply {
            val annotation = methodElement.getAnnotation(REFRESH::class.java)

            add("$classNetworkBuilder builder = new $classNetworkBuilder();")

            add("builder.setTypeToken(returnType);")

            add("builder.setRequestType($classRequestType.GET);")
            add("builder.setRepositoryClass(${ClassName.get(classElement.asType())}.class);")
            add("builder.setUrl(url);")
            add("builder.setRequiresLogin(${annotation.requiresLogin});")
            add("builder.setShowProgress(${annotation.showProgress});")
            add("builder.setFetchSecurityLayer(this);")
            add("builder.setInsertSql(insertSql);")
            add("builder.setSaveData(true);")

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

        }.joinToString(separator = "\n", postfix = "\n")
    }
}