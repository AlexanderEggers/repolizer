package repolizer.repository.method

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.DB
import repolizer.annotation.repository.util.DatabaseOperation
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryDBMethod {

    private val annotationRoomInsert = ClassName.get("android.arch.persistence.room", "Insert")
    private val annotationRoomUpdate = ClassName.get("android.arch.persistence.room", "Update")
    private val annotationRoomQuery = ClassName.get("android.arch.persistence.room", "Query")
    private val annotationRoomDelete = ClassName.get("android.arch.persistence.room", "Delete")
    private val classOnConflictStrategy = ClassName.get("android.arch.persistence.room", "OnConflictStrategy")

    private val classDatabaseBuilder = ClassName.get("repolizer.repository.database", "DatabaseBuilder")
    private val classDatabaseLayer = ClassName.get("repolizer.repository.database", "DatabaseLayer")

    fun build(element: Element, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        RepositoryMapHolder.dbAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val dbMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)

            val daoMethodBuilder = MethodSpec.methodBuilder("dbFor_${methodElement.simpleName}")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)

            val databaseOperation = methodElement.getAnnotation(DB::class.java).databaseOperation
            val sql = methodElement.getAnnotation(DB::class.java).sql

            val annotation = if (sql.isEmpty()) {
                when (databaseOperation) {
                    DatabaseOperation.INSERT -> AnnotationSpec.builder(annotationRoomInsert)
                            .addMember("onConflict", "$classOnConflictStrategy.REPLACE")
                            .build()
                    DatabaseOperation.UPDATE -> AnnotationSpec.builder(annotationRoomUpdate)
                            .addMember("onConflict", "$classOnConflictStrategy.REPLACE")
                            .build()
                    DatabaseOperation.DELETE -> AnnotationSpec.builder(annotationRoomDelete).build()
                    else -> throw IllegalStateException("If you want to use the " +
                            "DatabaseOperation.QUERY, you need to define the sql value as well.")
                }
            } else {
                AnnotationSpec.builder(annotationRoomQuery)
                        .addMember("value", "\"$sql\"")
                        .build()
            }
            daoMethodBuilder.addAnnotation(annotation)

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                dbMethodBuilder.addParameter(varType, varElement.simpleName.toString(), Modifier.FINAL)
            }

            val daoParamList = ArrayList<String>()
            RepositoryMapHolder.sqlParameterAnnotationMap["${element.simpleName}.${methodElement.simpleName}"]?.forEach {
                val elementType = ClassName.get(it.asType())
                daoMethodBuilder.addParameter(elementType, it.simpleName.toString())
                daoParamList.add(it.simpleName.toString())
            }

            val networkGetLayerClass = createDatabaseLayerAnonymousClass(methodElement.simpleName.toString(), daoParamList)
            dbMethodBuilder.addStatement("$classDatabaseBuilder builder = new $classDatabaseBuilder()")
            dbMethodBuilder.addStatement("builder.setDatabaseLayer($networkGetLayerClass)")
            dbMethodBuilder.addStatement("super.executeDB(builder)")

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