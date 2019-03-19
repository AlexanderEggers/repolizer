package repolizer.repository.method

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.REFRESH
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.repository.RepositoryMapHolder
import repolizer.repository.RepositoryProcessorUtil.Companion.buildSql
import repolizer.repository.RepositoryProcessorUtil.Companion.buildUrl
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

class RepositoryRefreshMethod {

    private val classNetworkRequest = ClassName.get("repolizer.repository.network", "NetworkFutureRequest")
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
                    if (sql.isNotEmpty()) addCode(buildSql(annotationMapKey,
                            "insertSql", sql))

                    //Uses the return type of the related GET method. This is needed for any
                    //storage related actions.
                    val relatedGetMethod = getGETExecutableElement(element, methodElement, url)
                    val returnTypeWithTypeToken = ParameterizedTypeName.get(classTypeToken,
                            ClassName.get(relatedGetMethod.returnType))
                    addStatement("$classTypeToken returnType = new $returnTypeWithTypeToken() {}")

                    val refreshMethodReturnTypeWithTypeToken = ParameterizedTypeName.get(classTypeToken,
                            ClassName.get(methodElement.returnType))
                    addStatement("$classTypeToken refreshMethodReturnType = new $refreshMethodReturnTypeWithTypeToken() {}")

                    addCode("\n")

                    //Generates the code which will be used for the NetworkBuilder to
                    //initialise it's values
                    addCode(getBuilderCode(annotationMapKey, element, methodElement))

                    addStatement("return super.executeRefresh(request, returnType.getType())")
                }.build()
            } ?: ArrayList())
        }
    }

    private fun getGETExecutableElement(element: Element, refreshMethod: ExecutableElement, url: String): ExecutableElement {
        RepositoryMapHolder.getAnnotationMap[element.simpleName.toString()]?.forEach {
            val getUrl = it.getAnnotation(GET::class.java).url
            if (getUrl == url) return it
        }
        throw IllegalStateException("@Refresh needs a @GET method as a reference. This reference is " +
                "build by comparing the url. Therefore you need to have to have a @GET method " +
                "that uses the same url then your refresh. Error for " +
                "${element.simpleName}.${refreshMethod.simpleName}")
    }

    private fun getBuilderCode(annotationMapKey: String, classElement: Element,
                               methodElement: ExecutableElement): String {
        return ArrayList<String>().apply {
            val annotation = methodElement.getAnnotation(REFRESH::class.java)

            add("$classNetworkRequest request = new $classNetworkRequest(this);")

            add("request.setTypeToken(refreshMethodReturnType);")

            add("request.setRequestType($classRequestType.GET);")
            add("request.setRepositoryClass(${ClassName.get(classElement.asType())}.class);")
            add("request.setUrl(url);")
            add("request.setRequiresLogin(${annotation.requiresLogin});")
            add("request.setInsertSql(insertSql);")
            add("request.setSaveData(true);")

            RepositoryMapHolder.headerAnnotationMap[annotationMapKey]?.forEach {
                add("request.addHeader(" +
                        "\"${it.getAnnotation(Header::class.java).key}\", ${it.simpleName});")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]?.forEach {
                add("request.addQuery(" +
                        "\"${it.getAnnotation(UrlQuery::class.java).key}\", ${it.simpleName});")
            }
        }.joinToString(separator = "\n", postfix = "\n")
    }
}