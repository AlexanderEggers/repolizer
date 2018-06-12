package repolizer.repository.method

import com.squareup.javapoet.*
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.RepositoryParameter
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.annotation.repository.util.ParameterType
import repolizer.repository.RepositoryMapHolder
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

class RepositoryGetMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classNetworkLayer = ClassName.get("repolizer.repository.network", "NetworkGetLayer")
    private val classCacheItem = ClassName.get("repolizer.database.cache", "CacheItem")
    private val classCacheState = ClassName.get("repolizer.database.cache", "CacheState")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")
    private val classList = ClassName.get(List::class.java)

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val classMutableLiveData = ClassName.get("android.arch.lifecycle", "MutableLiveData")
    private val classFunction = ClassName.get("android.arch.core.util", "Function")
    private val classTransformations = ClassName.get("android.arch.lifecycle", "Transformations")
    private val classAnnotationRoomInsert = ClassName.get("android.arch.persistence.room", "Insert")
    private val classAnnotationRoomQuery = ClassName.get("android.arch.persistence.room", "Query")
    private val classOnConflictStrategy = ClassName.get("android.arch.persistence.room", "OnConflictStrategy")

    fun build(messager: Messager, element: Element, entity: ClassName, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.getAnnotationMap[element.simpleName.toString()]?.map { methodElement ->
                //Collects all DB annotation related parameters from the source method. Those
                //values will be used to create the DAO method and to assign the correct values
                //to the database via the DatabaseResource/DatabaseLayer
                val daoParamList = getDaoParamList(element, methodElement, messager)

                //Creates DAO methods which will be used to communicate between this repository
                //and the database
                daoClassBuilder.addMethod(createDaoInsertMethod(methodElement, entity))
                daoClassBuilder.addMethod(createDaoQueryMethod(element, methodElement, entity, daoParamList))
                daoClassBuilder.addMethod(createDaoDeleteMethod(element, methodElement))

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
                    val url = methodElement.getAnnotation(GET::class.java).url
                    addStatement("String url = \"$url\"")
                    addCode(buildUrl(annotationMapKey))

                    //Generates the code which will be used for the NetworkBuilder to
                    //initialise it's values
                    addCode(getBuilderCode(annotationMapKey, methodElement, entity, daoParamList))

                    //Generates the code which is defined the @RepositoryParameter annotation.
                    //Those values are used to tweak the behavior of the repository regarding certain
                    //cases (like cannot refresh data due to error and cache is too old).
                    addCode(getRepositoryCode(element, annotationMapKey))

                    //Determine the return value and if it's correct used by the user
                    val getAsList = methodElement.getAnnotation(GET::class.java).getAsList
                    val classGenericTypeForMethod = if (getAsList) ParameterizedTypeName.get(classList, entity) else entity
                    val returnValue = ClassName.get(methodElement.returnType)

                    if (returnValue != ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod)) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Methods which are using the " +
                                "@GET annotation are only accepting LiveData<ENTITY> as a return type." +
                                "The ENTITY stands for the class which you have defined inside your" +
                                "@Repository annotation under the field entity. Error for " +
                                "${element.simpleName}.${methodElement.simpleName}")
                    }
                }.build()
            } ?: ArrayList())
        }
    }

    private fun createDaoInsertMethod(methodElement: Element, entity: ClassName): MethodSpec {
        return MethodSpec.methodBuilder("insertFor_${methodElement.simpleName}").apply {
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)

            val onConflictStrategy = methodElement.getAnnotation(GET::class.java).onConflictStrategy
            addAnnotation(AnnotationSpec.builder(classAnnotationRoomInsert).apply {
                addMember("onConflict", "$classOnConflictStrategy.$onConflictStrategy")
            }.build())

            val classArrayWithEntity = ArrayTypeName.of(entity)
            addParameter(classArrayWithEntity, "elements")
            varargs()
        }.build()
    }

    private fun createDaoQueryMethod(element: Element, methodElement: Element, entity: ClassName,
                                     daoParamList: ArrayList<VariableElement>): MethodSpec {
        val getAsList = methodElement.getAnnotation(GET::class.java).getAsList
        val classGenericTypeForMethod = if (getAsList) ParameterizedTypeName.get(classList, entity) else entity

        return MethodSpec.methodBuilder("queryFor_${methodElement.simpleName}").apply {
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            addAnnotation(AnnotationSpec.builder(classAnnotationRoomQuery).apply {
                var querySql = methodElement.getAnnotation(GET::class.java).querySql
                if (querySql.isEmpty()) {
                    val tableName = element.getAnnotation(Repository::class.java).tableName
                    querySql = "SELECT * FROM $tableName"
                }

                addMember("value", "\"$querySql\"")
            }.build())
            returns(ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod))

            daoParamList.forEach {
                val elementType = ClassName.get(it.asType())
                addParameter(elementType, it.simpleName.toString())
            }
        }.build()
    }

    private fun createDaoDeleteMethod(element: Element, methodElement: Element): MethodSpec {
        return MethodSpec.methodBuilder("deleteAllFor_${methodElement.simpleName}").apply {
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            addAnnotation(AnnotationSpec.builder(classAnnotationRoomQuery).apply {
                var deleteSql = methodElement.getAnnotation(GET::class.java).deleteSql
                if (deleteSql.isEmpty()) {
                    val tableName = element.getAnnotation(Repository::class.java).tableName
                    deleteSql = "DELETE FROM $tableName"
                }

                addMember("value", "\"$deleteSql\"")
            }.build())

        }.build()
    }

    private fun buildUrl(annotationMapKey: String): String {
        return ArrayList<String>().apply {
            addAll(RepositoryMapHolder.urlParameterAnnotationMap[annotationMapKey]?.map {
                "url = url.replace(\":${it.simpleName}\", \"$it\");"
            } ?: ArrayList())

            val queries = RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]
            if (queries?.isNotEmpty() == true) add(getFullUrlQueryPart(annotationMapKey))
        }.joinToString(separator = "\n", postfix = "\n\n")
    }

    private fun getFullUrlQueryPart(annotationMapKey: String): String {
        return ArrayList<String>().apply {
            RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]?.map { urlQuery ->
                add("url += " + "\"${urlQuery.getAnnotation(UrlQuery::class.java).key}=\" + ${urlQuery.simpleName};")
            } ?: ArrayList()
        }.joinToString(prefix = "url += \"?\";", separator = "\nurl += \"&\";\n")
    }

    private fun getBuilderCode(annotationMapKey: String, methodElement: Element, entity: ClassName,
                               daoParamList: ArrayList<VariableElement>): String {

        return ArrayList<String>().apply {
            val annotation = methodElement.getAnnotation(GET::class.java)

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
                    entity, annotation.maxCacheTime, annotation.maxFreshTime,
                    methodElement.simpleName.toString(), getAsList, daoParamList,
                    annotation.url.isEmpty())
            add("builder.setNetworkLayer($networkGetLayerClass);")
        }.joinToString(separator = "\n", postfix = "\n")
    }

    private fun getRepositoryCode(element: Element, annotationMapKey: String): String {
        return ArrayList<String>().apply {
            var allowFetchParamName: String? = null
            var deleteIfCacheIsTooOldParamName: String? = null

            RepositoryMapHolder.repositoryParameterAnnotationMap[annotationMapKey]?.forEach { variable ->
                val type = variable.getAnnotation(RepositoryParameter::class.java).type
                when (type) {
                    ParameterType.ALLOW_FETCH -> allowFetchParamName = variable.simpleName.toString()
                    ParameterType.DELETE_IF_CACHE_TOO_OLD -> deleteIfCacheIsTooOldParamName = variable.simpleName.toString()
                }
            }

            if (deleteIfCacheIsTooOldParamName != null) {
                add("builder.setDeletingCacheIfTooOld($deleteIfCacheIsTooOldParamName);")
            } else {
                val deleteIfCacheIsTooOldByDefault = element.getAnnotation(Repository::class.java).deleteIfCacheIsTooOld
                add("builder.setDeletingCacheIfTooOld($deleteIfCacheIsTooOldByDefault);")
            }

            if (allowFetchParamName != null) {
                add("return super.executeGet(builder, $allowFetchParamName);")
            } else {
                val allowFetchByDefault = element.getAnnotation(Repository::class.java).allowFetchByDefault
                add("return super.executeGet(builder, $allowFetchByDefault);")
            }
        }.joinToString(separator = "\n", postfix = "\n")
    }

    private fun createNetworkGetLayerAnonymousClass(classGenericTypeForMethod: TypeName, entity: ClassName,
                                                    maxCacheTime: Long, maxFreshTime: Long,
                                                    methodName: String, getAsList: Boolean,
                                                    daoParamList: ArrayList<VariableElement>,
                                                    isDBOnly: Boolean): TypeSpec {

        val daoInsertStatement = if (getAsList) {
            val classArrayWithEntity = ArrayTypeName.of(entity)
            "$classArrayWithEntity insertValue = value.toArray(new $entity[value.size()])"
        } else {
            "$entity insertValue = value"
        }

        return TypeSpec.anonymousClassBuilder("").apply {
            addSuperinterface(ParameterizedTypeName.get(classNetworkLayer, classGenericTypeForMethod))
            addMethod(MethodSpec.methodBuilder("updateDB")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(classGenericTypeForMethod, "value")
                    .addStatement(daoInsertStatement)
                    .addStatement("dataDao.insertFor_$methodName(insertValue)")
                    .build())

            if (isDBOnly) {
                addMethod(MethodSpec.methodBuilder("needsFetchByTime").apply {
                    addAnnotation(Override::class.java)
                    addModifiers(Modifier.PUBLIC)
                    addParameter(String::class.java, "fullUrlId")
                    addStatement("$classMutableLiveData livedata = new ${ParameterizedTypeName.get(classMutableLiveData, classCacheState)}")
                    addStatement("livedata.setValue($classCacheState.NEEDS_NO_REFRESH)")
                    addStatement("return livedata")
                    returns(ParameterizedTypeName.get(classLiveData, classCacheState))
                }.build())
            } else {
                addMethod(MethodSpec.methodBuilder("needsFetchByTime").apply {
                    addAnnotation(Override::class.java)
                    addModifiers(Modifier.PUBLIC)
                    addParameter(String::class.java, "fullUrlId")
                    addStatement("return $classTransformations.map(cacheDao.getCache(fullUrlId), " +
                            "${createTransformationMapFunctionClass(maxCacheTime, maxFreshTime)})")
                    returns(ParameterizedTypeName.get(classLiveData, classCacheState))
                }.build())
            }

            addMethod(MethodSpec.methodBuilder("updateFetchTime").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addParameter(String::class.java, "fullUrlId")
                addStatement("cacheDao.insert(new $classCacheItem(fullUrlId, System.currentTimeMillis()))")
            }.build())
            addMethod(MethodSpec.methodBuilder("getData").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addStatement("return ${createDaoCall(methodName, daoParamList)}")
                returns(ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod))
            }.build())
            addMethod(MethodSpec.methodBuilder("removeAllData").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addStatement("dataDao.deleteAllFor_$methodName()")
            }.build())
        }.build()
    }

    private fun createTransformationMapFunctionClass(maxCacheTime: Long, maxFreshTime: Long): TypeSpec {
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(classFunction, classCacheItem, classCacheState))
                .addMethod(MethodSpec.methodBuilder("apply")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(classCacheItem, "cacheItem")
                        .addStatement("if(cacheItem == null) return $classCacheState.NO_CACHE")
                        .addCode("\n")
                        .addStatement("long lifeSpanOfCache = System.currentTimeMillis() - cacheItem.getCacheTime()")
                        .addStatement("if(lifeSpanOfCache >= Long.parseLong(\"$maxFreshTime\")) return $classCacheState.NEEDS_SOFT_REFRESH")
                        .addStatement("else if(lifeSpanOfCache >= Long.parseLong(\"$maxCacheTime\")) return $classCacheState.NEEDS_HARD_REFRESH")
                        .addStatement("else return $classCacheState.NEEDS_NO_REFRESH")
                        .returns(classCacheState)
                        .build())
                .build()
    }

    private fun createDaoCall(methodName: String, daoParamList: ArrayList<VariableElement>): String {
        return daoParamList.joinToString(prefix = "dataDao.queryFor_$methodName(", postfix = ")") {
            it.simpleName.toString()
        }
    }

    private fun getDaoParamList(element: Element, methodElement: Element, messager: Messager): ArrayList<VariableElement> {
        return ArrayList<VariableElement>().apply {
            addAll(RepositoryMapHolder.sqlParameterAnnotationMap["${element.simpleName}" +
                    ".${methodElement.simpleName}"] ?: ArrayList())

            if (isEmpty()) {
                messager.printMessage(Diagnostic.Kind.NOTE, "The method " +
                        "${methodElement.simpleName} has no parameter and will always " +
                        "execute the same sql string. If that is your intention, you can " +
                        "ignore this note. Info for " +
                        "${element.simpleName}.${methodElement.simpleName}")
            }
        }
    }
}