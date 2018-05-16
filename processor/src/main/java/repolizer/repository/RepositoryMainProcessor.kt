package repolizer.repository

import com.squareup.javapoet.*
import repolizer.MainProcessor
import repolizer.annotation.repository.*
import repolizer.annotation.repository.parameter.*
import repolizer.repository.method.RepositoryDBMethod
import repolizer.repository.method.RepositoryGetMethod
import repolizer.util.AnnotationProcessor
import repolizer.util.ProcessorUtil.Companion.getGeneratedDatabaseDao
import repolizer.util.ProcessorUtil.Companion.getGeneratedDatabaseName
import repolizer.util.ProcessorUtil.Companion.getGeneratedRepositoryName
import repolizer.util.ProcessorUtil.Companion.getPackageName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException

class RepositoryMainProcessor : AnnotationProcessor {

    private val classBaseRepository = ClassName.get("repolizer.repository", "BaseRepository")
    private val classAppExecutor = ClassName.get("repolizer.repository.util", "AppExecutor")

    private val classGlobalDatabaseProvider = ClassName.get("repolizer.database", "GlobalDatabaseProvider")

    private val classRepolizer = ClassName.get("repolizer", "Repolizer")

    override fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        initRepositoryAnnotations(mainProcessor, roundEnv)

        roundEnv.getElementsAnnotatedWith(Repository::class.java).forEach {
            //Repository annotation general data
            val repositoryName = it.simpleName.toString()
            val repositoryPackageName = getPackageName(mainProcessor, it)
            val repositoryClassName = ClassName.get(repositoryPackageName, repositoryName)

            //Entity data for the generics param provided by the @Repository annotation
            val objectEntity: TypeElement = getRepositoryEntityClass(it.getAnnotation(Repository::class.java))!!.asElement() as TypeElement
            val classEntity = ClassName.get(getPackageName(mainProcessor, objectEntity), objectEntity.simpleName.toString())

            //Database data for the generics param provided by the @Repository annotation
            val objectDatabase = getRepositoryDatabaseClass(it.getAnnotation(Repository::class.java))!!.asElement() as TypeElement
            val classDatabase = ClassName.get(getPackageName(mainProcessor, objectDatabase), objectDatabase.simpleName.toString())
            val classRealDatabase = ClassName.get(getPackageName(mainProcessor, objectDatabase),
                    getGeneratedDatabaseName(objectDatabase.simpleName.toString()))
            val classDatabaseDao = ClassName.get(getPackageName(mainProcessor, objectDatabase),
                    getGeneratedDatabaseDao(objectDatabase.simpleName.toString(), objectEntity.simpleName.toString()))

            //General field data
            val classRepositoryParent: TypeName = ParameterizedTypeName.get(classBaseRepository,
                    classEntity)

            val fileBuilder = TypeSpec.classBuilder(getGeneratedRepositoryName(repositoryName))
                    .superclass(classRepositoryParent)
                    .addSuperinterface(repositoryClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addField(FieldSpec.builder(classRealDatabase, "db")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("$classGlobalDatabaseProvider.INSTANCE.getDatabase(super.getContext(), $classDatabase.class)")
                            .build())
                    .addField(FieldSpec.builder(classAppExecutor, "appExecutor")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("AppExecutor.INSTANCE")
                            .build())
                    .addField(FieldSpec.builder(classDatabaseDao, "dao")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("db.get" + objectEntity.simpleName + "Dao()")
                            .build())
                    .addMethod(MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(classRepolizer, "repolizer")
                            .addStatement("super(repolizer)")
                            .build())

            RepositoryGetMethod().build(it, classEntity, TypeSpec.classBuilder("DaoClassTest")).forEach {
                fileBuilder.addMethod(it)
            }

            RepositoryDBMethod().build(it, TypeSpec.classBuilder("DaoClassTest")).forEach {
                fileBuilder.addMethod(it)
            }

            val file = fileBuilder.build()
            JavaFile.builder(repositoryPackageName, file)
                    .build()
                    .writeTo(mainProcessor.filer)
        }
    }

    private fun initRepositoryAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        //Method annotations
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, REFRESH::class.java, RepositoryMapHolder.refreshAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, GET::class.java, RepositoryMapHolder.getAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, POST::class.java, RepositoryMapHolder.postAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, PUT::class.java, RepositoryMapHolder.putAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, DELETE::class.java, RepositoryMapHolder.deleteAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, DB::class.java, RepositoryMapHolder.dbAnnotationMap)

        //Param annotations
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, RepositoryParameter::class.java, RepositoryMapHolder.repositoryParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, Header::class.java, RepositoryMapHolder.headerAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, RequestBody::class.java, RepositoryMapHolder.requestBodyAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, SqlParameter::class.java, RepositoryMapHolder.sqlParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, UrlParameter::class.java, RepositoryMapHolder.urlParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, UrlQuery::class.java, RepositoryMapHolder.urlQueryAnnotationMap)
    }

    private fun getRepositoryEntityClass(annotation: Repository): DeclaredType? {
        try {
            annotation.entity
        } catch (ex: MirroredTypeException) {
            return ex.typeMirror as DeclaredType
        }
        return null
    }

    private fun getRepositoryDatabaseClass(annotation: Repository): DeclaredType? {
        try {
            annotation.database
        } catch (ex: MirroredTypeException) {
            return ex.typeMirror as DeclaredType
        }
        return null
    }
}