package repolizer.database

import com.squareup.javapoet.*
import repolizer.MainProcessor
import repolizer.ProcessorUtil
import repolizer.ProcessorUtil.Companion.getGeneratedDatabaseName
import repolizer.annotation.database.Database
import repolizer.annotation.database.Migration
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

class DatabaseMainProcessor {

    private val classCacheItem = ClassName.get("repolizer.database.cache", "CacheItem")
    private val classRepolizerDatabase = ClassName.get("repolizer.database", "RepolizerDatabase")

    private val classAnnotationDatabase = ClassName.get("android.arch.persistence.room", "Database")
    private val classAnnotationTypeConverters = ClassName.get("android.arch.persistence.room", "TypeConverters")

    fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        initMigrationAnnotations(mainProcessor, roundEnv)

        for (databaseElement in roundEnv.getElementsAnnotatedWith(Database::class.java)) {
            val typeElement = databaseElement as TypeElement

            //checks if the annotated @Database file has the correct file type
            if (!databaseElement.kind.isInterface) {
                mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                        "be applied to an interface. Error for ${typeElement.simpleName}")
                continue
            }

            //@Database does not support parent interface classes
            if (!typeElement.interfaces.isEmpty()) {
                mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Parent " +
                        "interfaces are not allowed. Error for ${typeElement.simpleName}")
                continue
            }

            //Database annotation general data
            val databaseName = databaseElement.simpleName.toString()
            val databasePackageName = ProcessorUtil.getPackageName(mainProcessor, databaseElement)
            val databaseClassName = ClassName.get(databasePackageName, databaseName)

            val realDatabaseName = getGeneratedDatabaseName(databaseName)
            val realDatabaseClassName = ClassName.get(databasePackageName, realDatabaseName)

            val version = databaseElement.getAnnotation(Database::class.java).version
            val exportSchema = databaseElement.getAnnotation(Database::class.java).exportSchema

            //Collecting and preparing entities for the room annotation
            val entities = DatabaseMapHolder.entityMap[databaseName] ?: ArrayList()
            val entitiesFormat: String = addEntityClassesToDatabase(entities)

            //Collecting and preparing dao classes for the room annotation
            val daoClasses = DatabaseMapHolder.daoMap[databaseName] ?: ArrayList()

            //Initialising database class and it's annotations for Room
            TypeSpec.classBuilder(getGeneratedDatabaseName(databaseName)).apply {
                superclass(classRepolizerDatabase)
                addSuperinterface(databaseClassName)
                addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)

                addAnnotation(AnnotationSpec.builder(classAnnotationDatabase)
                        .addMember("entities", "{$entitiesFormat}")
                        .addMember("version", "$version")
                        .addMember("exportSchema", "$exportSchema")
                        .build())

                val converterFormat = addConvertersToDatabase(databaseElement)
                converterFormat.run {
                    if (isNotEmpty()) {
                        addAnnotation(AnnotationSpec.builder(classAnnotationTypeConverters)
                                .addMember("value", "{$converterFormat}")
                                .build())
                    }
                }

                addDaoClassesToDatabase(daoClasses).forEach {
                    addMethod(it)
                }
            }.build().also {
                JavaFile.builder(databasePackageName, it)
                        .build()
                        .writeTo(mainProcessor.filer)
            }

            //Creation of the related database provider which will create and provide this
            //database via the GlobalDatabaseProvider
            createDatabaseProvider(mainProcessor.filer, databaseElement, databaseName,
                    realDatabaseClassName, databasePackageName)
        }
    }

    private fun createDatabaseProvider(filer: Filer, databaseElement: Element, databaseName: String,
                                       realDatabaseClassName: ClassName, databasePackageName: String) {
        DatabaseProvider().build(databaseElement, databaseName, realDatabaseClassName).also { file ->
            JavaFile.builder(databasePackageName, file)
                    .build()
                    .writeTo(filer)
        }
    }

    private fun addEntityClassesToDatabase(entities: ArrayList<ClassName>): String {
        return entities.joinToString(prefix = "$classCacheItem.class", separator = "") { entityName ->
            ", $entityName.class"
        }
    }

    private fun addDaoClassesToDatabase(daoList: ArrayList<ClassName>): List<MethodSpec> {
        return ArrayList<MethodSpec>().apply {
            daoList.forEach { daoClassName ->
                add(MethodSpec.methodBuilder("get${daoClassName.simpleName()}")
                        .apply {
                            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            returns(daoClassName)
                        }.build())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addConvertersToDatabase(element: Element): String {
        val converterList: ArrayList<String> = ArrayList()

        element.annotationMirrors.forEach { annotations ->
            annotations.elementValues.forEach { annotationValue ->
                val key = annotationValue.key.simpleName.toString()
                val value = annotationValue.value.value

                if (key == "typeConverter") {
                    val typeMirrors = value as List<AnnotationValue>
                    typeMirrors.forEach {
                        val declaredType = it.value as DeclaredType
                        val objectClass = declaredType.asElement()
                        converterList.add("$objectClass.class")
                    }
                }
            }
        }

        return converterList.joinToString(separator = ", ")
    }

    private fun initMigrationAnnotations(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        DatabaseMapHolder.migrationAnnotationMap.apply {
            for (databaseElement in roundEnv.getElementsAnnotatedWith(Migration::class.java)) {
                if (!databaseElement.kind.isInterface) {
                    mainProcessor.messager.printMessage(Diagnostic.Kind.ERROR, "Can only " +
                            "be applied to an interface. Error for ${databaseElement.simpleName}")
                    continue
                }

                val key = databaseElement.simpleName.toString()
                val currentList: ArrayList<Element> = get(key) ?: ArrayList()

                currentList.add(databaseElement)
                put(databaseElement.simpleName.toString(), currentList)
            }
        }
    }
}