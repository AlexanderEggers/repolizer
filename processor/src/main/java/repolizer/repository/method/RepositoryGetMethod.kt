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

class RepositoryGetMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classNetworkGetLayer = ClassName.get("repolizer.repository.network", "NetworkGetLayer")
    private val classRequestType = ClassName.get("repolizer.repository.util", "RequestType")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")
    private val classList = ClassName.get(List::class.java)

    private lateinit var classEntity: TypeName
    private lateinit var classArrayWithEntity: TypeName

    private var cacheTime: Long = Long.MAX_VALUE
    private var freshTime: Long = Long.MAX_VALUE

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val classAnnotationRoomInsert = ClassName.get("android.arch.persistence.room", "Insert")
    private val classAnnotationRoomQuery = ClassName.get("android.arch.persistence.room", "Query")
    private val classOnConflictStrategy = ClassName.get("android.arch.persistence.room", "OnConflictStrategy")

    fun build(element: Element, entity: ClassName, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        classEntity = entity
        classArrayWithEntity = ArrayTypeName.of(entity) //TODO use return type of method to determine correct type

        val tableName = element.getAnnotation(Repository::class.java).tableName
        cacheTime = element.getAnnotation(Repository::class.java).cacheTime
        freshTime = element.getAnnotation(Repository::class.java).freshTime

        RepositoryMapHolder.getAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
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
                            .addMember("value", querySql)
                            .build())
                    .returns(classGenericTypeForMethod)

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

            getMethodBuilder.addStatement("$classNetworkBuilder builder = new $classWithNetworkBuilder(new $classWithTypeToken() {})")
            getMethodBuilder.addStatement("builder.setRequestType($classRequestType.GET)")
            getMethodBuilder.addStatement("builder.setUrl(\"$url\")")
            getMethodBuilder.addStatement("builder.setRequiresLogin($requiresLogin)")
            getMethodBuilder.addStatement("builder.setShowProgress($showProgress)")

            RepositoryMapHolder.headerAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                getMethodBuilder.addStatement("builder.addHeader(${it.simpleName})")
            }

            RepositoryMapHolder.requestBodyAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                getMethodBuilder.addStatement("builder.setRaw(${it.simpleName})")
            }

            RepositoryMapHolder.urlQueryAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {
                getMethodBuilder.addStatement("builder.addQuery(${it.simpleName})")
            }

            val networkGetLayerClass = createNetworkGetLayerAnonymousClass(classGenericTypeForMethod,
                    cacheTime, freshTime, methodElement.simpleName.toString(), getAsList, daoParamList)
            getMethodBuilder.addStatement("builder.setNetworkLayer($networkGetLayerClass)")
            getMethodBuilder.addStatement("return super.executeGet(builder, true)") //TODO Add 'true' to method implementation >> add enum to differ between repo only params

            daoClassBuilder.addMethod(daoInsertMethodBuilder.build())
            daoClassBuilder.addMethod(daoQueryMethodBuilder.build())
            builderList.add(getMethodBuilder.build())
        }

        return builderList
    }

    private fun createNetworkGetLayerAnonymousClass(classGenericTypeForMethod: TypeName, cacheTime: Long,
                                                    freshTime: Long, methodName: String, getAsList: Boolean,
                                                    daoParamList: ArrayList<String>): TypeSpec {

        val daoInsertStatement = if(getAsList) {
            "$classArrayWithEntity insertValue = value.toArray(new $classEntity[value.size()])"
        } else {
            "$classGenericTypeForMethod insertValue = value"
        }

        var daoQueryCall = "dataDao.queryFor_$methodName("
        daoParamList.forEach {
            daoQueryCall += it
        }

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(classNetworkGetLayer, classGenericTypeForMethod))
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
                        .addStatement("return $daoQueryCall)")
                        .returns(ParameterizedTypeName.get(classLiveData, classGenericTypeForMethod))
                        .build())
                .build()
    }
}