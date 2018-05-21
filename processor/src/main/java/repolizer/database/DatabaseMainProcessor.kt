package repolizer.database

import com.squareup.javapoet.*
import repolizer.MainProcessor
import repolizer.annotation.database.Converter
import repolizer.annotation.database.Database
import repolizer.annotation.database.Migration
import repolizer.util.AnnotationProcessor
import repolizer.util.ProcessorUtil
import repolizer.util.ProcessorUtil.Companion.getGeneratedDatabaseName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Modifier

class DatabaseMainProcessor : AnnotationProcessor {

    private val classCacheDao = ClassName.get("repolizer.database.cache", "CacheDao")
    private val classRepolizerDatabase = ClassName.get("repolizer.database", "RepolizerDatabase")

    private val classAnnotationDatabase = ClassName.get("android.arch.persistence.room", "Database")

    override fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        initAnnotations(mainProcessor, roundEnv)

        roundEnv.getElementsAnnotatedWith(Database::class.java).forEach {
            //Database annotation general data
            val databaseName = it.simpleName.toString()
            val databasePackageName = ProcessorUtil.getPackageName(mainProcessor, it)
            val databaseClassName = ClassName.get(databasePackageName, databaseName)

            val realDatabaseName = getGeneratedDatabaseName(databaseName)
            val realDatabaseClassName = ClassName.get(databasePackageName, realDatabaseName)

            val version = it.getAnnotation(Database::class.java).version
            val exportSchema = it.getAnnotation(Database::class.java).exportSchema

            val fileBuilder = TypeSpec.classBuilder(getGeneratedDatabaseName(databaseName))
                    .superclass(classRepolizerDatabase)
                    .addSuperinterface(databaseClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)

            val entities = DatabaseMapHolder.daoMap[databaseName]
            val entitiesFormat: String = if (entities != null) addDaoClassesToDatabase(entities, fileBuilder)
            else addDaoClassesToDatabase(ArrayList(), fileBuilder)

            fileBuilder.addAnnotation(AnnotationSpec.builder(classAnnotationDatabase)
                    .addMember("entities", entitiesFormat)
                    .addMember("version", "$version")
                    .addMember("exportSchema", "$exportSchema")
                    .build())

            val providerFile = DatabaseProvider().build(mainProcessor, it, databaseName, realDatabaseClassName)
            JavaFile.builder(databasePackageName, providerFile)
                    .build()
                    .writeTo(mainProcessor.filer)

            val repoFile = fileBuilder.build()
            JavaFile.builder(databasePackageName, repoFile)
                    .build()
                    .writeTo(mainProcessor.filer)
        }
    }

    private fun initAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        DatabaseProcessorUtil.initClassAnnotations(mainProcessor, roundEnv, Converter::class.java, DatabaseMapHolder.converterAnnotationMap)
        DatabaseProcessorUtil.initClassAnnotations(mainProcessor, roundEnv, Migration::class.java, DatabaseMapHolder.migrationAnnotationMap)
    }

    private fun addDaoClassesToDatabase(daoList: ArrayList<ClassName>, fileBuilder: TypeSpec.Builder): String {
        var format = "{$classCacheDao.class"

        daoList.forEach { daoClassName ->
            format += ", $daoClassName.class"
            fileBuilder.addMethod(createDatabaseDaoFunction(daoClassName))
        }

        return "$format}"
    }

    private fun createDatabaseDaoFunction(daoClassName: ClassName): MethodSpec {
        return MethodSpec.methodBuilder("get${daoClassName.simpleName()}")
                .addModifiers(Modifier.ABSTRACT)
                .returns(daoClassName)
                .build()
    }
}