package repolizer.database

import com.squareup.javapoet.*
import repolizer.MainProcessor
import repolizer.annotation.database.Database
import repolizer.util.AnnotationProcessor
import repolizer.util.ProcessorUtil
import repolizer.util.ProcessorUtil.Companion.getGeneratedDatabaseName
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType

class DatabaseMainProcessor : AnnotationProcessor {

    private val classCacheDao = ClassName.get("repolizer.database.cache", "CacheDao")
    private val classRepolizerDatabase = ClassName.get("repolizer.database", "RepolizerDatabase")

    private val classAnnotationDatabase = ClassName.get("android.arch.persistence.room", "Database")
    private val classAnnotationTypeConverters = ClassName.get("android.arch.persistence.room", "TypeConverters")

    override fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
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

            val entities = DatabaseMapHolder.entityMap[databaseName]
            val entitiesFormat: String = if (entities != null) addEntityClassesToDatabase(entities)
            else addEntityClassesToDatabase(ArrayList())

            val daoClasses: ArrayList<ClassName>? = DatabaseMapHolder.daoMap[databaseName]
            addDaoClassesToDatabase(daoClasses, fileBuilder)

            fileBuilder.addAnnotation(AnnotationSpec.builder(classAnnotationDatabase)
                    .addMember("entities", entitiesFormat)
                    .addMember("version", "$version")
                    .addMember("exportSchema", "$exportSchema")
                    .build())

            val converterFormat = addConvertersToDatabase(it)
            if (!converterFormat.isEmpty()) {
                fileBuilder.addAnnotation(AnnotationSpec.builder(classAnnotationTypeConverters)
                        .addMember("value", converterFormat)
                        .build())
            }

            val providerFile = DatabaseProvider().build(it, databaseName, realDatabaseClassName)
            JavaFile.builder(databasePackageName, providerFile)
                    .build()
                    .writeTo(mainProcessor.filer)

            val repoFile = fileBuilder.build()
            JavaFile.builder(databasePackageName, repoFile)
                    .build()
                    .writeTo(mainProcessor.filer)
        }
    }

    private fun addEntityClassesToDatabase(daoList: ArrayList<ClassName>): String {
        var format = "{$classCacheDao.class"

        daoList.forEach { daoClassName ->
            format += ", $daoClassName.class"
        }

        return "$format}"
    }

    private fun addDaoClassesToDatabase(daoList: ArrayList<ClassName>?, fileBuilder: TypeSpec.Builder) {
        daoList?.forEach { daoClassName ->
            fileBuilder.addMethod(createDatabaseDaoFunction(daoClassName))
        }
    }

    private fun createDatabaseDaoFunction(daoClassName: ClassName): MethodSpec {
        return MethodSpec.methodBuilder("get${daoClassName.simpleName()}")
                .addModifiers(Modifier.ABSTRACT)
                .returns(daoClassName)
                .build()
    }

    @Suppress("UNCHECKED_CAST")
    private fun addConvertersToDatabase(element: Element): String {
        var converterFormat = ""
        element.annotationMirrors.forEach {
            it.elementValues.forEach {
                val key = it.key.simpleName.toString()
                val value = it.value.value

                if (key == "value") {
                    val typeMirrors = value as List<AnnotationValue>
                    typeMirrors.forEach {
                        val declaredType = it.value as DeclaredType
                        val objectClass = declaredType.asElement()

                        if (!converterFormat.isEmpty()) {
                            converterFormat += ", "
                        }
                        converterFormat += "$objectClass.class"
                    }
                }
            }
        }
        return "{$converterFormat}"
    }
}