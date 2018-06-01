package repolizer.repository

import com.squareup.javapoet.*
import repolizer.MainProcessor
import repolizer.annotation.repository.*
import repolizer.annotation.repository.parameter.*
import repolizer.database.DatabaseMapHolder
import repolizer.database.DatabaseProcessorUtil
import repolizer.repository.method.*
import repolizer.util.AnnotationProcessor
import repolizer.util.ProcessorUtil.Companion.getGeneratedDatabaseDaoName
import repolizer.util.ProcessorUtil.Companion.getGeneratedDatabaseName
import repolizer.util.ProcessorUtil.Companion.getGeneratedRepositoryName
import repolizer.util.ProcessorUtil.Companion.getPackageName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.tools.Diagnostic

class RepositoryMainProcessor : AnnotationProcessor {

    private val classBaseRepository = ClassName.get("repolizer.repository", "BaseRepository")
    private val classAppExecutor = ClassName.get("repolizer.repository.util", "AppExecutor")

    private val classGlobalDatabaseProvider = ClassName.get("repolizer.database.provider", "GlobalDatabaseProvider")
    private val classCacheDao = ClassName.get("repolizer.database.cache", "CacheDao")

    private val classRepolizer = ClassName.get("repolizer", "Repolizer")

    private val classAnnotationDao = ClassName.get("android.arch.persistence.room", "Dao")

    override fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        initAnnotations(mainProcessor, roundEnv)

        roundEnv.getElementsAnnotatedWith(Repository::class.java).forEach {
            if (it.kind != ElementKind.INTERFACE) {
                mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                        "be applied to an interface.")
                return
            }

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
                    getGeneratedDatabaseDaoName(objectDatabase.simpleName.toString(), objectEntity.simpleName.toString()))
            val daoName = getGeneratedDatabaseDaoName(objectDatabase.simpleName.toString(),
                    objectEntity.simpleName.toString())

            //General field data
            val classRepositoryParent: TypeName = ParameterizedTypeName.get(classBaseRepository,
                    classEntity)

            val fileBuilder = TypeSpec.classBuilder(getGeneratedRepositoryName(repositoryName))
                    .superclass(classRepositoryParent)
                    .addSuperinterface(repositoryClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addField(FieldSpec.builder(classAppExecutor, "appExecutor")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("AppExecutor.INSTANCE")
                            .build())
                    .addField(FieldSpec.builder(classRealDatabase, "db")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("$classGlobalDatabaseProvider.INSTANCE.getDatabase(super.getContext(), $classDatabase.class)")
                            .build())
                    .addField(FieldSpec.builder(classDatabaseDao, "dataDao")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("db.get$daoName()")
                            .build())
                    .addField(FieldSpec.builder(classCacheDao, "cacheDao")
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("db.getCacheDao()")
                            .build())
                    .addMethod(MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(classRepolizer, "repolizer")
                            .addStatement("super(repolizer)")
                            .build())

            DatabaseProcessorUtil.addClassNameToDatabaseMap(DatabaseMapHolder.daoMap,
                    objectDatabase.simpleName.toString(),
                    ClassName.get(getPackageName(mainProcessor, objectDatabase), daoName))

            DatabaseProcessorUtil.addClassNameToDatabaseMap(DatabaseMapHolder.entityMap,
                    objectDatabase.simpleName.toString(), classEntity)

            val daoBuilder = TypeSpec.interfaceBuilder(daoName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(classAnnotationDao)

            RepositoryRefreshMethod().build(it, classEntity, daoBuilder).forEach {
                fileBuilder.addMethod(it)
            }

            RepositoryGetMethod().build(it, classEntity, daoBuilder).forEach {
                fileBuilder.addMethod(it)
            }

            RepositoryCudMethod().build(it, classEntity).forEach {
                fileBuilder.addMethod(it)
            }

            RepositoryDBMethod().build(mainProcessor.messager, it, daoBuilder).forEach {
                fileBuilder.addMethod(it)
            }

            RepositoryCacheMethod().build(it).forEach {
                fileBuilder.addMethod(it)
            }

            val daoFile = daoBuilder.build()
            JavaFile.builder(getPackageName(mainProcessor, objectDatabase), daoFile)
                    .build()
                    .writeTo(mainProcessor.filer)

            val repoFile = fileBuilder.build()
            JavaFile.builder(repositoryPackageName, repoFile)
                    .build()
                    .writeTo(mainProcessor.filer)
        }
    }

    private fun initAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        //Method annotations
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, REFRESH::class.java, RepositoryMapHolder.refreshAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, GET::class.java, RepositoryMapHolder.getAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, CUD::class.java, RepositoryMapHolder.cudAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, DB::class.java, RepositoryMapHolder.dbAnnotationMap)
        RepositoryProcessorUtil.initMethodAnnotations(mainProcessor, roundEnv, CACHE::class.java, RepositoryMapHolder.cacheAnnotationMap)

        //Param annotations
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, RepositoryParameter::class.java, RepositoryMapHolder.repositoryParameterAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, Header::class.java, RepositoryMapHolder.headerAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, RequestBody::class.java, RepositoryMapHolder.requestBodyAnnotationMap)
        RepositoryProcessorUtil.initParamAnnotations(mainProcessor, roundEnv, DatabaseBody::class.java, RepositoryMapHolder.databaseBodyAnnotationMap)
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