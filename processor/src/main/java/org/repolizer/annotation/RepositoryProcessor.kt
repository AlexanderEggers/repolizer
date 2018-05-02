package org.repolizer.annotation

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import org.repolizer.MainProcessor
import org.repolizer.annotation.repository.DB
import org.repolizer.annotation.repository.Repository
import org.repolizer.annotation.repository.method.Header
import org.repolizer.annotation.repository.method.SQLParameter
import org.repolizer.util.AnnotationProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

class RepositoryProcessor : AnnotationProcessor {

    override fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        roundEnv.getElementsAnnotatedWith(Repository::class.java).forEach {
            val typeElement = it as TypeElement
            val repositoryName = typeElement.simpleName.toString()
            val repositoryPackageName = mainProcessor.elements!!.getPackageOf(typeElement).qualifiedName.toString()
            val repositoryClassName = ClassName.get(repositoryPackageName, repositoryName)

            val fileBuilder = TypeSpec.classBuilder("Generated_" + repositoryName + "Impl")
                    .addSuperinterface(repositoryClassName)
                    .addModifiers(Modifier.PUBLIC)

            roundEnv.getElementsAnnotatedWith(DB::class.java).forEach {
                val exElement = it as ExecutableElement

                val varElement = exElement.parameters[0] as VariableElement
                val varType = ClassName.get(varElement.asType())

                //check which Annotation is used by using getAnnotation(...) to test if the value is non-null
                //if the final value is still null, it means the parameter has no annotation is therefore an error

                fileBuilder.addMethod(MethodSpec.methodBuilder(exElement.simpleName.toString())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(varType, varElement.simpleName.toString())
                        .build())
            }

            val file = fileBuilder.build()
            JavaFile.builder(repositoryPackageName, file)
                    .build()
                    .writeTo(mainProcessor.filer)
        }
    }
}