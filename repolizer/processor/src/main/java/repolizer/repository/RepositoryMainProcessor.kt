package repolizer.repository

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import repolizer.MainProcessor
import repolizer.ProcessorUtil.Companion.getGeneratedRepositoryName
import repolizer.ProcessorUtil.Companion.getPackageName
import repolizer.annotation.repository.*
import repolizer.annotation.repository.parameter.*
import repolizer.repository.method.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class RepositoryMainProcessor {

    private val classRepolizer = ClassName.get("repolizer", "Repolizer")
    private val classBaseRepository = ClassName.get("repolizer.repository", "BaseRepository")

    fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        initAnnotations(mainProcessor, roundEnv)

        roundEnv.getElementsAnnotatedWith(Repository::class.java).forEach { repositoryElement ->
            val typeElement = repositoryElement as TypeElement

            //checks if the annotated @Repository file has the correct file type
            if (!repositoryElement.kind.isInterface) {
                mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "@Repository can " +
                        "only be applied to an interface. Error for ${typeElement.simpleName}")
            }

            //@Repository does not support parent interface classes
            if (typeElement.interfaces.isNotEmpty()) {
                mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Parent " +
                        "interfaces are not allowed. Error for ${typeElement.simpleName}")
            }

            //Repository annotation general data
            val repositoryName = repositoryElement.simpleName.toString()
            val repositoryPackageName = getPackageName(mainProcessor, repositoryElement)
            val repositoryClassName = ClassName.get(repositoryPackageName, repositoryName)

            //Initialising repository class including needed fields and constructor
            TypeSpec.classBuilder(getGeneratedRepositoryName(repositoryName)).apply {
                //Class general configs
                superclass(classBaseRepository)
                addSuperinterface(repositoryClassName)
                addModifiers(Modifier.PUBLIC, Modifier.FINAL)

                //Class constructor
                addMethod(MethodSpec.constructorBuilder().apply {
                    addModifiers(Modifier.PUBLIC)
                    addParameter(classRepolizer, "repolizer")
                    addStatement("super(repolizer)")
                }.build())

                //Methods
                getRepositoryMethods(repositoryElement).forEach {
                    addMethod(it)
                }
            }.build().also { repoFile ->
                //create repository class file
                JavaFile.builder(repositoryPackageName, repoFile)
                        .build()
                        .writeTo(mainProcessor.filer)
            }
        }
    }

    private fun getRepositoryMethods(repositoryClassElement: Element): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            addAll(RepositoryRefreshMethod().build(repositoryClassElement))
            addAll(RepositoryGetMethod().build(repositoryClassElement))
            addAll(RepositoryCudMethod().build(repositoryClassElement))
            addAll(RepositoryDataMethod().build(repositoryClassElement))
            addAll(RepositoryCacheMethod().build(repositoryClassElement))
        }
    }

    private fun initAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        //Method annotations
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv,
                REFRESH::class.java, RepositoryMapHolder.refreshAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv,
                GET::class.java, RepositoryMapHolder.getAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv,
                CUD::class.java, RepositoryMapHolder.cudAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv,
                DATA::class.java, RepositoryMapHolder.dbAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv,
                CACHE::class.java, RepositoryMapHolder.cacheAnnotationMap)

        //Param annotations
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                RepositoryParameter::class.java, false, RepositoryMapHolder.repositoryParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                Header::class.java, false, RepositoryMapHolder.headerAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                RequestBody::class.java, true, RepositoryMapHolder.requestBodyAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                DataBody::class.java, true, RepositoryMapHolder.dataBodyAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                CacheBody::class.java, true, RepositoryMapHolder.cacheBodyAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                StatementParameter::class.java, false, RepositoryMapHolder.statementParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                UrlParameter::class.java, false, RepositoryMapHolder.urlParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                UrlQuery::class.java, false, RepositoryMapHolder.urlQueryAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv,
                MultipartBody::class.java, false, RepositoryMapHolder.multipartBodyAnnotationMap)
    }
}