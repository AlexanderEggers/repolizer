package repolizer.repository.method

import com.squareup.javapoet.*
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.repository.RepositoryMapHolder
import java.lang.reflect.ParameterizedType
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType

class RepositoryGetMethod {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classNetworkGetLayer = ClassName.get("repolizer.repository.network", "NetworkGetLayer")
    private val classRequestType = ClassName.get("repolizer.repository.util", "RequestType")

    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")

    private val classAnnotationRoomInsert = ClassName.get("android.arch.persistence.room", "Insert")
    private val classAnnotationRoomQuery = ClassName.get("android.arch.persistence.room", "Query")
    private val classOnConflictStrategy = ClassName.get("android.arch.persistence.room", "OnConflictStrategy")

    fun build(element: Element, entity: ClassName, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()
        val classArrayWithEntity: TypeName = ArrayTypeName.of(entity) //TODO use return type of method to determine correct type

        val cacheTime = element.getAnnotation(Repository::class.java).cacheTime
        val refreshTime = element.getAnnotation(Repository::class.java).freshTime

        RepositoryMapHolder.getAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val getMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)
                    .returns(ClassName.get(methodElement.returnType))

            val classReturnType = ParameterizedTypeName.get(methodElement.returnType as ParameterizedType).rawType
            val classTypeTokenWithEntity: TypeName = ParameterizedTypeName.get(classTypeToken,
                    classReturnType)
            val classNetworkBuilderWithEntity: TypeName = ParameterizedTypeName.get(classNetworkBuilder,
                    classReturnType)

            val daoInsertMethodBuilder = MethodSpec.methodBuilder("insertFor" + methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(classAnnotationRoomInsert)
                            .addMember("onConflict", "$classOnConflictStrategy.REPLACE") //TODO put inside annotation
                            .build())
                    .addParameter(classArrayWithEntity, "elements")
                    .varargs()
            val daoQueryMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(classAnnotationRoomQuery)
                            .addMember("value", methodElement.getAnnotation(GET::class.java).sql)
                            .build())
                    .returns(classReturnType)

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                getMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val daoParamList = ArrayList<String>()
            RepositoryMapHolder.sqlParameterAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {

                val elementType = ClassName.get(it.asType())
                daoQueryMethodBuilder.addParameter(elementType, it.simpleName.toString())
                daoParamList.add(it.simpleName.toString())
            }

            var daoCall = "dao.${methodElement.simpleName}("
            daoParamList.forEach {
                daoCall += it
            }

            val url = methodElement.getAnnotation(GET::class.java).url
            val requiresLogin = methodElement.getAnnotation(GET::class.java).requiresLogin
            val showProgress = methodElement.getAnnotation(GET::class.java).showProgress

            getMethodBuilder.addStatement("$classNetworkBuilder builder = new $classNetworkBuilderWithEntity(new $classTypeTokenWithEntity() {})")
            getMethodBuilder.addStatement("builder.setRequestType($classRequestType.GET)")
            getMethodBuilder.addStatement("builder.setUrl($url)")
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

            getMethodBuilder.addCode("builder.setNetworkLayer(new $classNetworkGetLayer() {\n\n" +
                    "   @Override\n" +
                    "   public void updateDB($classReturnType entities) {\n" +
                    "       $daoCall);\n" +
                    "   }\n\n" +
                    "   @Override\n" +
                    "   public boolean needsFetchByTime(String fullUrlId) {\n" +
                    "       $daoCall);\n" +
                    "   }\n\n" +
                    "   @Override\n" +
                    "   public void updateFetchTime(String fullUrlId) {\n" +
                    "       $daoCall);\n" +
                    "   }\n\n" +
                    "   @Override\n" +
                    "   public $classReturnType getData() {\n" +
                    "       $daoCall);\n" +
                    "   }\n" +
                    "});\n")
            getMethodBuilder.addStatement("return super.executeGet(builder, true)")

            daoClassBuilder.addMethod(daoInsertMethodBuilder.build())
            daoClassBuilder.addMethod(daoQueryMethodBuilder.build())
            builderList.add(getMethodBuilder.build())
        }

        return builderList
    }
}