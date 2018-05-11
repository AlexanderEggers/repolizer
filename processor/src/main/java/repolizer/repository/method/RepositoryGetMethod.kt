package repolizer.repository.method

import com.squareup.javapoet.*
import repolizer.repository.RepositoryMapHolder
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class RepositoryGetMethod(daoClassBuilder: TypeSpec.Builder) {

    private val classNetworkBuilder = ClassName.get("repolizer.repository.network", "NetworkBuilder")
    private val classTypeToken = ClassName.get("com.google.gson.reflect", "TypeToken")

    fun build(element: Element): List<MethodSpec.Builder> {
        val builderList = ArrayList<MethodSpec.Builder>()

        RepositoryMapHolder.dbAnnotationMap[element.simpleName.toString()]?.forEach { methodElement ->
            val dbMethodBuilder = MethodSpec.methodBuilder(methodElement.simpleName.toString())
                    .addModifiers(Modifier.PUBLIC)

            methodElement.parameters.forEach { varElement ->
                val varType = ClassName.get(varElement.asType())
                dbMethodBuilder.addParameter(varType, varElement.simpleName.toString())
            }

            val returnType = ClassName.get(methodElement.returnType)
            val classRepositoryParent: TypeName = ParameterizedTypeName.get(classNetworkBuilder, returnType)
            val classTypeToken = ParameterizedTypeName.get(classTypeToken, returnType)

            dbMethodBuilder.addStatement("$classNetworkBuilder networkBuilder = new $classRepositoryParent(new $classTypeToken<List<SimcardBaseModel>>() {})")

            RepositoryMapHolder.sqlParameterAnnotationMap[methodElement.simpleName.toString()]?.forEach {

            }

            builderList.add(dbMethodBuilder)
        }




        return builderList
    }
}