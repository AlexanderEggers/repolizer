package repolizer.repository.method

import com.squareup.javapoet.*
import repolizer.annotation.repository.DB
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryDBMethod {

    private val annotationRoomQuery = ClassName.get("android.arch.persistence.room", "Query")

    private val classDatabaseBuilder = ClassName.get("repolizer.repository.database", "DatabaseBuilder")
    private val classDatabaseLayer = ClassName.get("repolizer.repository.database", "DatabaseLayer")

    fun build(element: Element, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        RepositoryMapHolder.dbAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val dbMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override::class.java)

            val daoMethodBuilder = MethodSpec.methodBuilder("queryFor_${methodElement.simpleName}")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(annotationRoomQuery)
                            .addMember("value", methodElement.getAnnotation(DB::class.java).sql)
                            .build())

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
        var daoQueryCall = "dataDao.queryFor_$methodName("
        val iterator = daoParamList.iterator()
        while(iterator.hasNext()) {
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