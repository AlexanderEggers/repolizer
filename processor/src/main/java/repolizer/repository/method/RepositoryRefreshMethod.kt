package repolizer.repository.method

import com.squareup.javapoet.*
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.REFRESH
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.repository.RepositoryMapHolder
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic

class RepositoryRefreshMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classNetworkLayer = ClassName.get("repolizer.repository.network", "NetworkRefreshLayer")
    private val classCacheItem = ClassName.get("repolizer.database.cache", "CacheItem")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")
    private val classList = ClassName.get(List::class.java)

    private val classAnnotationRoomInsert = ClassName.get("android.arch.persistence.room", "Insert")
    private val classOnConflictStrategy = ClassName.get("android.arch.persistence.room", "OnConflictStrategy")

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val liveDataOfBoolean = ParameterizedTypeName.get(classLiveData, ClassName.get(java.lang.Boolean::class.java))

    fun build(messager: Messager, element: Element, entity: ClassName, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {

        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.refreshAnnotationMap[element.simpleName.toString()]?.map { methodElement ->
                //Creates DAO method which will be used to communicate between this repository
                //and the database
                daoClassBuilder.addMethod(createDaoMethod(methodElement, entity))

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

                    //Generates the code which will be used for the NetworkBuilder to
                    //initialise it's values
                    addCode(getBuilderCode(annotationMapKey, methodElement, entity))

                    //Determine the return value and if it's correct used by the user
                    val returnValue = ClassName.get(methodElement.returnType)
                    if (returnValue == liveDataOfBoolean) {
                        addStatement("return super.executeRefresh(builder)")
                    } else {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Methods which are using the " +
                                "@REFRESH annotation are only accepting LiveData<Boolean> as a return type." +
                                "Error for ${element.simpleName}.${methodElement.simpleName}")
                    }
                }.build()
            } ?: ArrayList())
        }
    }

    private fun createDaoMethod(methodElement: Element, entity: ClassName): MethodSpec {
        return MethodSpec.methodBuilder("insertFor_${methodElement.simpleName}").apply {
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)

            val onConflictStrategy = methodElement.getAnnotation(REFRESH::class.java).onConflictStrategy
            addAnnotation(AnnotationSpec.builder(classAnnotationRoomInsert).apply {
                addMember("onConflict", "$classOnConflictStrategy.$onConflictStrategy")
            }.build())

            val classArrayWithEntity = ArrayTypeName.of(entity)
            addParameter(classArrayWithEntity, "elements")
            varargs()
        }.build()
    }

    private fun buildUrl(annotationMapKey: String): String {
        return ArrayList<String>().apply {
            addAll(RepositoryMapHolder.urlParameterAnnotationMap[annotationMapKey]?.map {
                "url = url.replace(\":${it.simpleName}\", \"$it\");"
            } ?: ArrayList())

            val queries = RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]
            if(queries?.isNotEmpty() == true) add(getFullUrlQueryPart(annotationMapKey))
        }.joinToString(separator = "\n", postfix = "\n\n")
    }

    private fun getFullUrlQueryPart(annotationMapKey: String): String {
        return ArrayList<String>().apply {
            RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]?.map { urlQuery ->
                add("url += " + "\"${urlQuery.getAnnotation(UrlQuery::class.java).key}=\" + ${urlQuery.simpleName};")
            } ?: ArrayList()
        }.joinToString(prefix = "url += \"?\";", separator = "\nurl += \"&\";\n")
    }

    private fun getBuilderCode(annotationMapKey: String, methodElement: Element, entity: ClassName): String {

        return ArrayList<String>().apply {
            val annotation = methodElement.getAnnotation(REFRESH::class.java)

            val getAsList = annotation.getAsList
            val classGenericTypeForMethod = if (getAsList) ParameterizedTypeName.get(classList, entity) else entity
            val classWithTypeToken = ParameterizedTypeName.get(classTypeToken, classGenericTypeForMethod)
            val classWithNetworkBuilder = ParameterizedTypeName.get(classNetworkBuilder, classGenericTypeForMethod)

            add("$classWithNetworkBuilder builder = new $classNetworkBuilder();")

            add("builder.setTypeToken(new $classWithTypeToken() {});")
            add("builder.setUrl(url);")
            add("builder.setRequiresLogin(${annotation.requiresLogin});")
            add("builder.setShowProgress(${annotation.showProgress});")

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

            val networkGetLayerClass = createNetworkGetLayerAnonymousClass(classGenericTypeForMethod,
                    methodElement.simpleName.toString(), entity, getAsList)
            add("builder.setNetworkLayer($networkGetLayerClass);")
        }.joinToString(separator = "\n", postfix = "\n")
    }

    private fun createNetworkGetLayerAnonymousClass(classGenericTypeForMethod: TypeName, methodName: String,
                                                    entity: ClassName, getAsList: Boolean): TypeSpec {
        val daoInsertStatement = if (getAsList) {
            val classArrayWithEntity = ArrayTypeName.of(entity)
            "$classArrayWithEntity insertValue = value.toArray(new $entity[value.size()])"
        } else {
            "$entity insertValue = value"
        }

        return TypeSpec.anonymousClassBuilder("").apply {
            addSuperinterface(ParameterizedTypeName.get(classNetworkLayer, classGenericTypeForMethod))
            addMethod(MethodSpec.methodBuilder("updateDB").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addParameter(classGenericTypeForMethod, "value")
                addStatement(daoInsertStatement)
                addStatement("dataDao.insertFor_$methodName(insertValue)")
            }.build())
            addMethod(MethodSpec.methodBuilder("updateFetchTime").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addParameter(String::class.java, "fullUrlId")
                addStatement("cacheDao.insert(new $classCacheItem(fullUrlId, System.currentTimeMillis()))")
            }.build())
        }.build()
    }
}