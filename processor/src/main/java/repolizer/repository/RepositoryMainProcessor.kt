package repolizer.repository

import com.squareup.javapoet.*
import repolizer.MainProcessor
import repolizer.annotation.repository.*
import repolizer.annotation.repository.parameter.*
import repolizer.util.AnnotationProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

class RepositoryMainProcessor : AnnotationProcessor {

    private val classClass = ClassName.get("repolizer.util", "BaseRepository")

    override fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        initRepositoryAnnotations(mainProcessor, roundEnv)

        roundEnv.getElementsAnnotatedWith(Repository::class.java).forEach {
            val typeElement = it as TypeElement
            val repositoryName = typeElement.simpleName.toString()
            val repositoryPackageName = mainProcessor.elements!!.getPackageOf(typeElement).qualifiedName.toString()
            val repositoryClassName = ClassName.get(repositoryPackageName, repositoryName)

            val classRepositoryEntity = ClassName.get(it.getAnnotation(Repository::class.java).entity.java)
            val classRepositoryParent: TypeName = ParameterizedTypeName.get(classClass,
                    classRepositoryEntity)

            val fileBuilder = TypeSpec.classBuilder("Generated_" + repositoryName + "Impl")
                    .superclass(classRepositoryParent)
                    .addSuperinterface(repositoryClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

            roundEnv.getElementsAnnotatedWith(DB::class.java).forEach {
                val exElement = it as ExecutableElement

                val varElement = exElement.parameters[0] as VariableElement
                val varType = ClassName.get(varElement.asType())

                //check which Annotation is used by using getAnnotation(...) to test if the value is non-null
                //if the final value is still null, it means the parameter has no annotation is therefore an error

                //OR prepare parameter annotations and save them inside map using RepoClassName.methodName as key and the element as value.

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

    private fun initRepositoryAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        //Method annotations
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, DB::class.java, RepositoryMapHolder.dbAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, DELETE::class.java, RepositoryMapHolder.deleteAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, GET::class.java, RepositoryMapHolder.getAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, POST::class.java, RepositoryMapHolder.postAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, PUT::class.java, RepositoryMapHolder.putAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, REFRESH::class.java, RepositoryMapHolder.refreshAnnotationMap)

        //Param annotations
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, RepositoryParameter::class.java, RepositoryMapHolder.repositoryParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, DatabaseBody::class.java, RepositoryMapHolder.databaseBodyAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, Header::class.java, RepositoryMapHolder.headerAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, RequestBody::class.java, RepositoryMapHolder.requestBodyAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, SqlParameter::class.java, RepositoryMapHolder.sqlParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, UrlParameter::class.java, RepositoryMapHolder.urlParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, UrlQuery::class.java, RepositoryMapHolder.urlQueryAnnotationMap)
    }
}