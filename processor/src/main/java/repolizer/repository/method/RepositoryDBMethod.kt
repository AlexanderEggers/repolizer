package repolizer.repository.method

import com.squareup.javapoet.*
import repolizer.annotation.repository.DB
import repolizer.annotation.repository.util.DatabaseOperation
import repolizer.repository.RepositoryMapHolder
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
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
    private val liveDataOfBoolean = ParameterizedTypeName.get(classLiveData, ClassName.get(java.lang.Boolean::class.java))

    fun build(messager: Messager, element: Element, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryMapHolder.dbAnnotationMap[element.simpleName.toString()]
                    ?.map { methodElement ->
                        //Collects all DB annotation related parameters from the source method. Those
                        //values will be used to create the DAO method and to assign the correct values
                        //to the database via the DatabaseResource/DatabaseLayer
                        val daoParamList = getDaoParamList(element, methodElement, messager)

                        //Creates DAO method which will be used to communicate between this repository
                        //and the database
                        daoClassBuilder.addMethod(createDaoMethod(messager, element, methodElement, daoParamList))

                        MethodSpec.methodBuilder(methodElement.simpleName.toString()).apply {
                            addModifiers(Modifier.PUBLIC)
                            addAnnotation(Override::class.java)

                            //Copy all interface parameter to the method implementation
                            methodElement.parameters.forEach { varElement ->
                                val varType = ClassName.get(varElement.asType())
                                addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
                            }

                            //Creates the database layer which will talk to the DAO
                            val networkGetLayerClass = createDatabaseLayerAnonymousClass(
                                    methodElement.simpleName.toString(), daoParamList)

                            addStatement("$classDatabaseBuilder builder = new $classDatabaseBuilder()")
                            addStatement("builder.setDatabaseLayer($networkGetLayerClass)")

                            //Determine the return value and if it's correct used by the user
                            val returnValue = ClassName.get(methodElement.returnType)
                            when {
                                returnValue == liveDataOfBoolean -> {
                                    returns(ClassName.get(methodElement.returnType))
                                    addStatement("return super.executeDB(builder)")
                                }
                                methodElement.returnType.kind == TypeKind.VOID -> addStatement("super.executeDB(builder)")
                                else -> messager.printMessage(Diagnostic.Kind.ERROR, "Methods which are using the " +
                                        "@DB annotation are only accepting LiveData<Boolean> or void as a return " +
                                        "type. Error for ${element.simpleName}.${methodElement.simpleName}")
                            }
                        }.build()
                    } ?: ArrayList())
        }
    }

    private fun createDaoMethod(messager: Messager, element: Element, methodElement: Element,
                                daoParamList: ArrayList<VariableElement>): MethodSpec {
        return MethodSpec.methodBuilder("dbFor_${methodElement.simpleName}").apply {
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)

            val databaseOperation = methodElement.getAnnotation(DB::class.java).databaseOperation
            val sql = methodElement.getAnnotation(DB::class.java).sql
            addAnnotation(getDaoMethodAnnotation(messager, element, methodElement,
                    databaseOperation, sql))

            daoParamList.forEach {
                val elementType = ClassName.get(it.asType())
                addParameter(elementType, it.simpleName.toString())
            }
        }.build()
    }

    private fun getDaoMethodAnnotation(messager: Messager, element: Element, methodElement: Element,
                                       databaseOperation: DatabaseOperation, sql: String?): AnnotationSpec {
        return (if (sql?.isEmpty() == true) {
            when (databaseOperation) {
                DatabaseOperation.INSERT -> {
                    AnnotationSpec.builder(annotationRoomInsert)
                            .addMember("onConflict", "$classOnConflictStrategy.REPLACE")
                            .build()
                }
                DatabaseOperation.UPDATE -> {
                    AnnotationSpec.builder(annotationRoomUpdate)
                            .addMember("onConflict", "$classOnConflictStrategy.REPLACE")
                            .build()
                }
                DatabaseOperation.DELETE -> {
                    AnnotationSpec.builder(annotationRoomDelete).build()
                }
                DatabaseOperation.QUERY -> {
                    messager.printMessage(Diagnostic.Kind.ERROR, "If you want to use the " +
                            "DatabaseOperation.QUERY, you need to define the sql value as well." +
                            "Error for ${element.simpleName}.${methodElement.simpleName}")

                    AnnotationSpec.builder(annotationRoomQuery)
                            .addMember("value", "\"$sql\"")
                            .build()
                }
            }
        } else {
            AnnotationSpec.builder(annotationRoomQuery)
                    .addMember("value", "\"$sql\"")
                    .build()
        })
    }

    private fun createDatabaseLayerAnonymousClass(methodName: String,
                                                  daoParamList: ArrayList<VariableElement>): TypeSpec {
        val daoQueryCall = daoParamList.joinToString(prefix = "dataDao.dbFor_$methodName(", postfix = ")") {
            it.simpleName.toString()
        }

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(classDatabaseLayer)
                .addMethod(MethodSpec.methodBuilder("updateDB")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement(daoQueryCall)
                        .build())
                .build()
    }

    private fun getDaoParamList(element: Element, methodElement: Element, messager: Messager): ArrayList<VariableElement> {
        return ArrayList<VariableElement>().apply {
            val objectExpected = methodElement.getAnnotation(DB::class.java).databaseOperation.objectExpected
            if (objectExpected) {
                addAll(RepositoryMapHolder.databaseBodyAnnotationMap["${element.simpleName}" +
                        ".${methodElement.simpleName}"] ?: ArrayList())

                if (isEmpty()) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "The method " +
                            "${methodElement.simpleName} needs to have at least one parameter " +
                            "which is using the @DatabaseBody annotation. Error for " +
                            "${element.simpleName}.${methodElement.simpleName}")
                }
            } else {
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
}