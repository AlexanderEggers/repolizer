package repolizer.repository.method

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.repository.DB
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryDBMethod {

    private val annotationRoomQuery = ClassName.get("android.arch.persistence.room", "Query")

    fun build(element: Element, daoClassBuilder: TypeSpec.Builder): List<MethodSpec> {
        val builderList = ArrayList<MethodSpec>()

        RepositoryMapHolder.dbAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val dbMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)

            val daoMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(annotationRoomQuery)
                            .addMember("value", methodElement.getAnnotation(DB::class.java).sql)
                            .build())

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                dbMethodBuilder.addParameter(varType, varElement.simpleName.toString())
            }

            val daoParamList = ArrayList<String>()
            RepositoryMapHolder.sqlParameterAnnotationMap[element.simpleName.toString() + "." +
                    methodElement.simpleName.toString()]?.forEach {

                val elementType = ClassName.get(it.asType())
                daoMethodBuilder.addParameter(elementType, it.simpleName.toString())
                daoParamList.add(it.simpleName.toString())
            }

            var daoCall = "dao.${methodElement.simpleName}("
            daoParamList.forEach {
                daoCall += it
            }
            dbMethodBuilder.addStatement("$daoCall)")

            daoClassBuilder.addMethod(daoMethodBuilder.build())
            builderList.add(dbMethodBuilder.build())
        }

        return builderList
    }
}