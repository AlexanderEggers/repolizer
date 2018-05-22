package repolizer.repository.method

import com.squareup.javapoet.*
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.REFRESH
import repolizer.annotation.repository.parameter.Header
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryRefreshMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classNetworkLayer = ClassName.get("repolizer.repository.network", "NetworkRefreshLayer")
    private val classRequestType = ClassName.get("repolizer.repository.util", "RequestType")
    private val classCacheItem = ClassName.get("repolizer.database.cache", "CacheItem")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")
    private val classList = ClassName.get(List::class.java)

    private val classAnnotationRoomInsert = ClassName.get("android.arch.persistence.room", "Insert")
    private val classOnConflictStrategy = ClassName.get("android.arch.persistence.room", "OnConflictStrategy")

    private lateinit var classEntity: TypeName
    private lateinit var classArrayWithEntity: TypeName

    fun build(element: Element, entity: ClassName, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        classEntity = entity
        classArrayWithEntity = ArrayTypeName.of(entity)

        RepositoryMapHolder.refreshAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val getAsList = methodElement.getAnnotation(REFRESH::class.java).getAsList
            val classGenericTypeForMethod = if (getAsList) ParameterizedTypeName.get(classList, entity) else entity

            val refreshMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
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

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                refreshMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val url = methodElement.getAnnotation(REFRESH::class.java).url
            val requiresLogin = methodElement.getAnnotation(REFRESH::class.java).requiresLogin
            val showProgress = methodElement.getAnnotation(REFRESH::class.java).showProgress

            val classWithTypeToken = ParameterizedTypeName.get(classTypeToken, classGenericTypeForMethod)
            val classWithNetworkBuilder = ParameterizedTypeName.get(classNetworkBuilder, classGenericTypeForMethod)

            refreshMethodBuilder.addStatement("String url = \"$url\"")
            RepositoryMapHolder.urlParameterAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                refreshMethodBuilder.addStatement("url = url.replace(\":${it.simpleName}\", \"$it\")")
            }
            refreshMethodBuilder.addCode("\n")

            refreshMethodBuilder.addStatement("$classNetworkBuilder builder = new $classWithNetworkBuilder()")
            refreshMethodBuilder.addStatement("builder.setTypeToken(new $classWithTypeToken() {})")
            refreshMethodBuilder.addStatement("builder.setRequestType($classRequestType.REFRESH)")
            refreshMethodBuilder.addStatement("builder.setUrl(url)")
            refreshMethodBuilder.addStatement("builder.setRequiresLogin($requiresLogin)")
            refreshMethodBuilder.addStatement("builder.setShowProgress($showProgress)")

            RepositoryMapHolder.headerAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                refreshMethodBuilder.addStatement("builder.addHeader(" +
                        "\"${it.getAnnotation(Header::class.java).key}\", ${it.simpleName})")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                refreshMethodBuilder.addStatement("builder.addQuery(" +
                        "\"${it.getAnnotation(UrlQuery::class.java).key}\", ${it.simpleName})")
            }

            val networkGetLayerClass = createNetworkGetLayerAnonymousClass(classGenericTypeForMethod,
                    methodElement.simpleName.toString(), getAsList)
            refreshMethodBuilder.addStatement("builder.setNetworkLayer($networkGetLayerClass)")
            refreshMethodBuilder.addStatement("return super.executeRefresh(builder)")

            daoClassBuilder.addMethod(daoInsertMethodBuilder.build())
            builderList.add(refreshMethodBuilder.build())
        }

        return builderList
    }

    private fun createNetworkGetLayerAnonymousClass(classGenericTypeForMethod: TypeName, methodName: String,
                                                    getAsList: Boolean): TypeSpec {

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
                .addMethod(MethodSpec.methodBuilder("updateFetchTime")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String::class.java, "fullUrlId")
                        .addStatement("cacheDao.insert(new $classCacheItem(fullUrlId, System.currentTimeMillis()))")
                        .build())
                .build()
    }
}