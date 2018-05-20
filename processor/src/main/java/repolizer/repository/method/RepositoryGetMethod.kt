package repolizer.repository.method

import com.squareup.javapoet.*
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.parameter.RepositoryParameter
import repolizer.annotation.repository.util.ParameterType

class RepositoryGetMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classNetworkLayer = ClassName.get("repolizer.repository.network", "NetworkGetLayer")
    private val classRequestType = ClassName.get("repolizer.repository.util", "RequestType")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")
    private val classList = ClassName.get(List::class.java)

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val classAnnotationRoomInsert = ClassName.get("android.arch.persistence.room", "Insert")
    private val classAnnotationRoomQuery = ClassName.get("android.arch.persistence.room", "Query")
    private val classOnConflictStrategy = ClassName.get("android.arch.persistence.room", "OnConflictStrategy")

    private lateinit var classEntity: TypeName
    private lateinit var classArrayWithEntity: TypeName

    fun build(element: Element, entity: ClassName, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()
        val tableName = element.getAnnotation(Repository::class.java).tableName

        classEntity = entity
        classArrayWithEntity = ArrayTypeName.of(entity)

        RepositoryMapHolder.getAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val maxCacheTime = methodElement.getAnnotation(GET::class.java).maxCacheTime
            val maxFreshTime = methodElement.getAnnotation(GET::class.java).maxFreshTime

            val getAsList = methodElement.getAnnotation(GET::class.java).getAsList
            val classGenericTypeForMethod = if (getAsList) ParameterizedTypeName.get(classList, entity) else entity

            val getMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)
                    .returns(ClassName.get(methodElement.returnType))

            val daoInsertMethodBuilder = MethodSpec.methodBuilder("insertFor_${methodElement.simpleName}")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(classAnnotationRoomInsert)
                            .addMember("onConflict", "$classOnConflictStrategy.REPLACE") //TODO put inside annotation
                            .build())
                    .addParameter(classArrayWithEntity, "elements")
                    .varargs()

            var querySql = methodElement.getAnnotation(GET::class.java).sql
            if(querySql == "") {
                querySql = "SELECT * FROM $tableName"
            }

            val daoQueryMethodBuilder = MethodSpec.methodBuilder("queryFor_${methodElement.simpleName}")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(classAnnotationRoomQuery)
                            .addMember("value", "\"$querySql\"")
                            .build())
                    .returns(ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod))

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                getMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val daoParamList = ArrayList<String>()
            RepositoryMapHolder.sqlParameterAnnotationMap["${element.simpleName}.${methodElement.simpleName}"]?.forEach {
                val elementType = ClassName.get(it.asType())
                daoQueryMethodBuilder.addParameter(elementType, it.simpleName.toString())
                daoParamList.add(it.simpleName.toString())
            }

            val url = methodElement.getAnnotation(GET::class.java).url
            val requiresLogin = methodElement.getAnnotation(GET::class.java).requiresLogin
            val showProgress = methodElement.getAnnotation(GET::class.java).showProgress

            val classWithTypeToken = ParameterizedTypeName.get(classTypeToken, classGenericTypeForMethod)
            val classWithNetworkBuilder = ParameterizedTypeName.get(classNetworkBuilder, classGenericTypeForMethod)

            getMethodBuilder.addStatement("$classNetworkBuilder builder = new $classWithNetworkBuilder()")
            getMethodBuilder.addStatement("builder.setTypeToken(new $classWithTypeToken() {})")
            getMethodBuilder.addStatement("builder.setRequestType($classRequestType.GET)")
            getMethodBuilder.addStatement("builder.setUrl(\"$url\")")
            getMethodBuilder.addStatement("builder.setRequiresLogin($requiresLogin)")
            getMethodBuilder.addStatement("builder.setShowProgress($showProgress)")

            RepositoryMapHolder.headerAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                getMethodBuilder.addStatement("builder.addHeader(${it.simpleName})")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                getMethodBuilder.addStatement("builder.addQuery(${it.simpleName})")
            }

            var allowFetchParamName: String? = null

            RepositoryMapHolder.repositoryParameterAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach { variable ->
                val type = variable.getAnnotation(RepositoryParameter::class.java).type
                when(type) {
                    ParameterType.ALLOW_FETCH -> allowFetchParamName = variable.simpleName.toString()
                }
            }

            val networkGetLayerClass = createNetworkGetLayerAnonymousClass(classGenericTypeForMethod,
                    maxCacheTime, maxFreshTime, methodElement.simpleName.toString(), getAsList, daoParamList)
            getMethodBuilder.addStatement("builder.setNetworkLayer($networkGetLayerClass)")

            if(allowFetchParamName != null) {
                getMethodBuilder.addStatement("return super.executeGet(builder, $allowFetchParamName)")
            } else {
                val allowFetchByDefault = element.getAnnotation(Repository::class.java).allowFetchByDefault
                getMethodBuilder.addStatement("return super.executeGet(builder, $allowFetchByDefault)")
            }

            daoClassBuilder.addMethod(daoInsertMethodBuilder.build())
            daoClassBuilder.addMethod(daoQueryMethodBuilder.build())
            builderList.add(getMethodBuilder.build())
        }

        return builderList
    }

    private fun createNetworkGetLayerAnonymousClass(classGenericTypeForMethod: TypeName, maxCacheTime: Long,
                                                    maxFreshTime: Long, methodName: String, getAsList: Boolean,
                                                    daoParamList: ArrayList<String>): TypeSpec {

        val daoInsertStatement = if(getAsList) {
            "$classArrayWithEntity insertValue = value.toArray(new $classEntity[value.size()])"
        } else {
            "$classGenericTypeForMethod insertValue = value"
        }

        var daoQueryCall = "dataDao.queryFor_$methodName("
        val iterator = daoParamList.iterator()
        while(iterator.hasNext()) {
            daoQueryCall += iterator.next()
            daoQueryCall += if (iterator.hasNext()) ", " else ""
        }
        daoQueryCall += ")"

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
                        .addStatement("return true")
                        .returns(Boolean::class.java)
                        .build())
                .addMethod(MethodSpec.methodBuilder("updateFetchTime")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String::class.java, "fullUrlId")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getData")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return $daoQueryCall")
                        .returns(ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod))
                        .build())
                .build()
    }
}