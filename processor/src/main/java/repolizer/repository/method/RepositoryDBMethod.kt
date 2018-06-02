package repolizer.repository.method

import com.squareup.javapoet.*
import repolizer.annotation.repository.DB
import repolizer.annotation.repository.util.DatabaseOperation
import repolizer.repository.RepositoryMapHolder
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic

class RepositoryDBMethod {

    private val annotationRoomInsert = ClassName.get("android.arch.persistence.room", "Insert")
    private val annotationRoomUpdate = ClassName.get("android.arch.persistence.room", "Update")
    private val annotationRoomQuery = ClassName.get("android.arch.persistence.room", "Query")
    private val annotationRoomDelete = ClassName.get("android.arch.persistence.room", "Delete")
    private val classOnConflictStrategy = ClassName.get("android.arch.persistence.room", "OnConflictStrategy")

    private val classDatabaseBuilder = ClassName.get("repolizer.repository.database", "DatabaseBuilder")
    private val classDatabaseLayer = ClassName.get("repolizer.repository.database", "DatabaseLayer")

    private val classLiveData = ClassName.get("android.arch.lifecycle", "LiveData")
    private val liveDataOfBoolean = ParameterizedTypeName.get(classLiveData, ClassName.get(Boolean::class.java))

    fun build(messager: Messager, element: Element, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        RepositoryMapHolder.dbAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val dbMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)

            val daoMethodBuilder = MethodSpec.methodBuilder("dbFor_${methodElement.simpleName}")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)

            val databaseOperation = methodElement.getAnnotation(DB::class.java).databaseOperation
            val sql = methodElement.getAnnotation(DB::class.java).sql
            var objectExpected = false

            val annotation = (if (sql.isEmpty()) {
                when (databaseOperation) {
                    DatabaseOperation.INSERT -> {
                        objectExpected = true
                        AnnotationSpec.builder(annotationRoomInsert)
                                .addMember("onConflict", "$classOnConflictStrategy.REPLACE")
                                .build()
                    }
                    DatabaseOperation.UPDATE -> {
                        objectExpected = true
                        AnnotationSpec.builder(annotationRoomUpdate)
                                .addMember("onConflict", "$classOnConflictStrategy.REPLACE")
                                .build()
                    }
                    DatabaseOperation.DELETE -> {
                        objectExpected = true
                        AnnotationSpec.builder(annotationRoomDelete).build()
                    }
                    else -> {
                        messager.printMessage(Diagnostic.Kind.WARNING, "If you want to use the " +
                                "DatabaseOperation.QUERY, you need to define the sql value as well.")
                        null
                    }
                }
            } else {
                AnnotationSpec.builder(annotationRoomQuery)
                        .addMember("value", "\"$sql\"")
                        .build()
            }) ?: return emptyList()
            daoMethodBuilder.addAnnotation(annotation)

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                dbMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val daoParamList = ArrayList<String>()
            if (objectExpected) {
                RepositoryMapHolder.databaseBodyAnnotationMap["${element.simpleName}.${methodElement.simpleName}"]?.forEach {
                    val elementType = ClassName.get(it.asType())
                    daoMethodBuilder.addParameter(elementType, it.simpleName.toString())
                    daoParamList.add(it.simpleName.toString())
                }

                if (daoParamList.isEmpty()) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "The method " +
                            "${methodElement.simpleName} needs to have at least one parameter " +
                            "which is using the @DatabaseBody annotation.")
                    return emptyList()
                }
            } else {
                RepositoryMapHolder.sqlParameterAnnotationMap["${element.simpleName}.${methodElement.simpleName}"]?.forEach {
                    val elementType = ClassName.get(it.asType())
                    daoMethodBuilder.addParameter(elementType, it.simpleName.toString())
                    daoParamList.add(it.simpleName.toString())
                }

                if (daoParamList.isEmpty()) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "The method " +
                            "${methodElement.simpleName} has no parameter and will always " +
                            "execute the same sql string. If that is your intention, you can " +
                            "ignore this note.")
                }
            }

            val networkGetLayerClass = createDatabaseLayerAnonymousClass(methodElement.simpleName.toString(), daoParamList)
            dbMethodBuilder.addStatement("$classDatabaseBuilder builder = new $classDatabaseBuilder()")
            dbMethodBuilder.addStatement("builder.setDatabaseLayer($networkGetLayerClass)")

            val returnValue = ClassName.get(methodElement.returnType)
            if(returnValue == liveDataOfBoolean) {
                dbMethodBuilder.returns(ClassName.get(methodElement.returnType))
                dbMethodBuilder.addStatement("return super.executeDB(builder)")
            } else if(methodElement.returnType == TypeKind.VOID){
                dbMethodBuilder.addStatement("super.executeDB(builder)")
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "Methods which are using the " +
                        "@DB annotation are only accepting LiveData<Boolean> or Void as a return " +
                        "type.")
                return emptyList()
            }

            daoClassBuilder.addMethod(daoMethodBuilder.build())
            builderList.add(dbMethodBuilder.build())
        }

        return builderList
    }

    private fun createDatabaseLayerAnonymousClass(methodName: String, daoParamList: ArrayList<String>): TypeSpec {
        var daoQueryCall = "dataDao.dbFor_$methodName("
        val iterator = daoParamList.iterator()
        while (iterator.hasNext()) {
            daoQueryCall += iterator.next()
            daoQueryCall += if (iterator.hasNext()) ", " else ""
        }
        daoQueryCall += ")"

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(classDatabaseLayer)
                .addMethod(MethodSpec.methodBuilder("updateDB")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement(daoQueryCall)
                        .build())
                .build()
    }
}