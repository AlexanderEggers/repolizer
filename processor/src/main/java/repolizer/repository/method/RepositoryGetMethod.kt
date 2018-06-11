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

    private lateinit var classEntity: TypeName
    private lateinit var classArrayWithEntity: TypeName

    fun build(messager: Messager, element: Element, entity: ClassName, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()
        val tableName = element.getAnnotation(Repository::class.java).tableName

        classEntity = entity
        classArrayWithEntity = ArrayTypeName.of(entity)

        val list = RepositoryMapHolder.getAnnotationMap[element.simpleName.toString()]
                ?: ArrayList()
        for (methodElement in list) {
            val getAsList = methodElement.getAnnotation(GET::class.java).getAsList
            val classGenericTypeForMethod = if (getAsList) ParameterizedTypeName.get(classList, entity) else entity

            val annotationMapKey = "${element.simpleName}.${methodElement.simpleName}"

            val getMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)
                    .returns(ClassName.get(methodElement.returnType))

            val daoInsertMethodBuilder = MethodSpec.methodBuilder("insertFor_${methodElement.simpleName}")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(classAnnotationRoomInsert)
                            .addMember("onConflict", "$classOnConflictStrategy.REPLACE")
                            .build())
                    .addParameter(classArrayWithEntity, "elements")
                    .varargs()

            var querySql = methodElement.getAnnotation(GET::class.java).querySql
            if (querySql.isEmpty()) {
                querySql = "SELECT * FROM $tableName"
            }

            val daoQueryMethodBuilder = MethodSpec.methodBuilder("queryFor_${methodElement.simpleName}")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(classAnnotationRoomQuery)
                            .addMember("value", "\"$querySql\"")
                            .build())
                    .returns(ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod))

            var deleteSql = methodElement.getAnnotation(GET::class.java).deleteSql
            if (deleteSql.isEmpty()) {
                deleteSql = "DELETE FROM $tableName"
            }

            val daoDeleteAllMethodBuilder = MethodSpec.methodBuilder("deleteAllFor_${methodElement.simpleName}")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(classAnnotationRoomQuery)
                            .addMember("value", "\"$deleteSql\"")
                            .build())

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                getMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val daoParamList = ArrayList<String>()
            RepositoryMapHolder.sqlParameterAnnotationMap[annotationMapKey]?.forEach {
                val elementType = ClassName.get(it.asType())
                daoQueryMethodBuilder.addParameter(elementType, it.simpleName.toString())
                daoParamList.add(it.simpleName.toString())
            }

            val url = methodElement.getAnnotation(GET::class.java).url
            val requiresLogin = methodElement.getAnnotation(GET::class.java).requiresLogin
            val showProgress = methodElement.getAnnotation(GET::class.java).showProgress

            val classWithTypeToken = ParameterizedTypeName.get(classTypeToken, classGenericTypeForMethod)
            val classWithNetworkBuilder = ParameterizedTypeName.get(classNetworkBuilder, classGenericTypeForMethod)

            getMethodBuilder.addStatement("String url = \"$url\"")
            RepositoryMapHolder.urlParameterAnnotationMap[annotationMapKey]?.forEach {
                getMethodBuilder.addStatement("url = url.replace(\":${it.simpleName}\", \"$it\")")
            }

            val urlQueryList = RepositoryMapHolder.urlQueryAnnotationMap[annotationMapKey]
            if (urlQueryList?.isEmpty() == false) {
                getMethodBuilder.addStatement("url += \"?\"")

                val iterator = urlQueryList.iterator()
                while (iterator.hasNext()) {
                    val urlQuery = iterator.next()
                    getMethodBuilder.addStatement("url += " +
                            "\"${urlQuery.getAnnotation(UrlQuery::class.java).key}=\" + ${urlQuery.simpleName}")

                    if (iterator.hasNext()) {
                        getMethodBuilder.addStatement("url += \"&\"")
                    }
                }
            }

            getMethodBuilder.addCode("\n")

            getMethodBuilder.addStatement("$classWithNetworkBuilder builder = new $classNetworkBuilder()")
            getMethodBuilder.addStatement("builder.setTypeToken(new $classWithTypeToken() {})")
            getMethodBuilder.addStatement("builder.setUrl(url)")
            getMethodBuilder.addStatement("builder.setRequiresLogin($requiresLogin)")
            getMethodBuilder.addStatement("builder.setShowProgress($showProgress)")

            RepositoryMapHolder.headerAnnotationMap[annotationMapKey]?.forEach {
                getMethodBuilder.addStatement("builder.addHeader(" +
                        "\"${it.getAnnotation(Header::class.java).key}\", ${it.simpleName})")
            }

            urlQueryList?.forEach {
                getMethodBuilder.addStatement("builder.addQuery(" +
                        "\"${it.getAnnotation(UrlQuery::class.java).key}\", ${it.simpleName})")
            }

            RepositoryMapHolder.progressParamsAnnotationMap[annotationMapKey]?.forEach {
                getMethodBuilder.addStatement("builder.setProgressParams(${it.simpleName})")
            }

            var allowFetchParamName: String? = null
            var deleteIfCacheIsTooOldParamName: String? = null

            RepositoryMapHolder.repositoryParameterAnnotationMap[annotationMapKey]?.forEach { variable ->
                val type = variable.getAnnotation(RepositoryParameter::class.java).type
                when (type) {
                    ParameterType.ALLOW_FETCH -> allowFetchParamName = variable.simpleName.toString()
                    ParameterType.DELETE_IF_CACHE_TOO_OLD -> deleteIfCacheIsTooOldParamName = variable.simpleName.toString()
                }
            }

            val networkGetLayerClass = if (url.isEmpty()) {
                createNetworkGetLayerAnonymousClassForDBOnly(classGenericTypeForMethod,
                        methodElement.simpleName.toString(), daoParamList)
            } else {
                val maxCacheTime = methodElement.getAnnotation(GET::class.java).maxCacheTime
                val maxFreshTime = methodElement.getAnnotation(GET::class.java).maxFreshTime

                createNetworkGetLayerAnonymousClass(classGenericTypeForMethod,
                        maxCacheTime, maxFreshTime, methodElement.simpleName.toString(), getAsList, daoParamList)
            }
            getMethodBuilder.addStatement("builder.setNetworkLayer($networkGetLayerClass)")

            if (deleteIfCacheIsTooOldParamName != null) {
                getMethodBuilder.addStatement("builder.setDeletingCacheIfTooOld($deleteIfCacheIsTooOldParamName)")
            } else {
                val deleteIfCacheIsTooOldByDefault = element.getAnnotation(Repository::class.java).deleteIfCacheIsTooOld
                getMethodBuilder.addStatement("builder.setDeletingCacheIfTooOld($deleteIfCacheIsTooOldByDefault)")
            }

            if (allowFetchParamName != null) {
                getMethodBuilder.addStatement("return super.executeGet(builder, $allowFetchParamName)")
            } else {
                val allowFetchByDefault = element.getAnnotation(Repository::class.java).allowFetchByDefault
                getMethodBuilder.addStatement("return super.executeGet(builder, $allowFetchByDefault)")
            }

            val returnValue = ClassName.get(methodElement.returnType)
            if (returnValue != ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Methods which are using the " +
                        "@GET annotation are only accepting LiveData<ENTITY> as a return type." +
                        "The ENTITY stands for the class which you have defined inside your" +
                        "@Repository annotation under the field entity. Error for " +
                        "${element.simpleName}.${methodElement.simpleName}")
                continue
            }

            daoClassBuilder.addMethod(daoInsertMethodBuilder.build())
            daoClassBuilder.addMethod(daoQueryMethodBuilder.build())
            daoClassBuilder.addMethod(daoDeleteAllMethodBuilder.build())
            builderList.add(getMethodBuilder.build())
        }

        return builderList
    }

    private fun createNetworkGetLayerAnonymousClassForDBOnly(classGenericTypeForMethod: TypeName,
                                                             methodName: String,
                                                             daoParamList: ArrayList<String>): TypeSpec {
        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(classNetworkLayer, classGenericTypeForMethod))
                .addMethod(MethodSpec.methodBuilder("updateDB")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(classGenericTypeForMethod, "value")
                        .addStatement("dataDao.insertFor_$methodName(insertValue)")
                        .build())
                .addMethod(MethodSpec.methodBuilder("needsFetchByTime")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String::class.java, "fullUrlId")
                        .addStatement("$classMutableLiveData livedata = new ${ParameterizedTypeName.get(classMutableLiveData, classCacheState)}")
                        .addStatement("livedata.setValue($classCacheState.NEEDS_NO_REFRESH)")
                        .addStatement("return livedata")
                        .returns(ParameterizedTypeName.get(classLiveData, classCacheState))
                        .build())
                .addMethod(MethodSpec.methodBuilder("updateFetchTime")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String::class.java, "fullUrlId")
                        .addStatement("cacheDao.insert(new $classCacheItem(fullUrlId, System.currentTimeMillis()))")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getData")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return ${createDaoCall(methodName, daoParamList)}")
                        .returns(ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod))
                        .build())
                .addMethod(MethodSpec.methodBuilder("removeAllData")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("dataDao.deleteAllFor_$methodName()")
                        .build())
                .build()
    }

    private fun createNetworkGetLayerAnonymousClass(classGenericTypeForMethod: TypeName, maxCacheTime: Long,
                                                    maxFreshTime: Long, methodName: String, getAsList: Boolean,
                                                    daoParamList: ArrayList<String>): TypeSpec {

        val daoInsertStatement = if (getAsList) {
            "$classArrayWithEntity insertValue = value.toArray(new $classEntity[value.size()])"
        } else {
            "$classGenericTypeForMethod insertValue = value"
        }

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(classNetworkLayer, classGenericTypeForMethod))
                .addMethod(MethodSpec.methodBuilder("updateDB")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(classGenericTypeForMethod, "value")
                        .addStatement(daoInsertStatement)
                        .addStatement("dataDao.insertFor_$methodName(insertValue)")
                        .build())
                .addMethod(MethodSpec.methodBuilder("needsFetchByTime")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String::class.java, "fullUrlId")
                        .addStatement("return $classTransformations.map(cacheDao.getCache(fullUrlId), " +
                                "${createTransformationMapFunctionClass(maxCacheTime, maxFreshTime)})")
                        .returns(ParameterizedTypeName.get(classLiveData, classCacheState))
                        .build())
                .addMethod(MethodSpec.methodBuilder("updateFetchTime")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String::class.java, "fullUrlId")
                        .addStatement("cacheDao.insert(new $classCacheItem(fullUrlId, System.currentTimeMillis()))")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getData")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return ${createDaoCall(methodName, daoParamList)}")
                        .returns(ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod))
                        .build())
                .addMethod(MethodSpec.methodBuilder("removeAllData")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("dataDao.deleteAllFor_$methodName()")
                        .build())
                .build()
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

    private fun createDaoCall(methodName: String, daoParamList: ArrayList<String>): String {
        var daoQueryCall = "dataDao.queryFor_$methodName("
        val iterator = daoParamList.iterator()
        while (iterator.hasNext()) {
            daoQueryCall += iterator.next()
            daoQueryCall += if (iterator.hasNext()) ", " else ""
        }
        return "$daoQueryCall)"
    }
}