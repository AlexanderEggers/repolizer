package repolizer.room

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import repolizer.ProcessorUtil.Companion.getGeneratedDatabaseProviderName
import repolizer.annotation.database.Database
import repolizer.annotation.database.Migration
import repolizer.annotation.database.util.DatabaseType
import repolizer.annotation.database.util.JournalMode
import repolizer.annotation.database.util.MigrationType
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType

class DatabaseProvider {

    private val classDatabaseProvider = ClassName.get("repolizer.database.provider", "DatabaseProvider")

    private val classContext = ClassName.get("android.content", "Context")

    private val classRoom = ClassName.get("android.arch.persistence.room", "Room")
    private val classRoomDatabase = ClassName.get("android.arch.persistence.room", "RoomDatabase")
    private val classRoomDatabaseBuilder = ClassName.get("android.arch.persistence.room.RoomDatabase", "Builder")
    private val classJournalMode = ClassName.get("android.arch.persistence.room.RoomDatabase", "JournalMode")

    fun build(element: Element, databaseName: String, realDatabaseClassName: ClassName): TypeSpec {
        return TypeSpec.classBuilder(getGeneratedDatabaseProviderName(databaseName)).apply {
            addSuperinterface(classDatabaseProvider)
            addModifiers(Modifier.PUBLIC)

            //Method that overrides the required getDatabase which will be used to create a
            //certain RoomDatabase instance
            addMethod(MethodSpec.methodBuilder("getDatabase").apply {
                addAnnotation(Override::class.java)
                addModifiers(Modifier.PUBLIC)
                addParameter(classContext, "context")
                returns(classRoomDatabase)

                addStatement(getDatabaseType(element, realDatabaseClassName))
                addStatement("builder.setJournalMode(${getJournalMode(element)})")
                addCode(addMigrationToMethod(element))

                addCode(getDestructiveMigration(element))

                addStatement("return builder.build()")
            }.build())
        }.build()
    }

    private fun getDatabaseType(element: Element, realDatabaseClassName: ClassName): String {
        val databaseType = element.getAnnotation(Database::class.java).type
        return if (databaseType == DatabaseType.PERSISTENT) {
            val databaseFileName = element.getAnnotation(Database::class.java).name
            "$classRoomDatabaseBuilder builder = $classRoom.databaseBuilder(context, $realDatabaseClassName.class, \"$databaseFileName\")"
        } else {
            "$classRoomDatabaseBuilder builder = $classRoom.inMemoryDatabaseBuilder(context, $realDatabaseClassName.class)"
        }
    }

    private fun getJournalMode(element: Element): String {
        val journalMode = element.getAnnotation(Database::class.java).journalMode
        return when (journalMode) {
            JournalMode.AUTOMATIC -> "$classJournalMode.AUTOMATIC"
            JournalMode.TRUNCATE -> "$classJournalMode.TRUNCATE"
            JournalMode.WRITE_AHEAD_LOGGING -> "$classJournalMode.WRITE_AHEAD_LOGGING"
        }
    }

    private fun addMigrationToMethod(element: Element): String {
        return ArrayList<String>().apply {
            val migrationFormat = getMigrationFormat(element)
            if (migrationFormat.isNotEmpty()) {
                add("\n")
                add("try {\n")
                add("     builder.addMigrations($migrationFormat);\n")
                add("} catch(InstantiationException|IllegalAccessException e) {\n")
                add("     e.printStackTrace();\n")
                add("}\n\n")
            }
        }.joinToString(separator = "")
    }

    @Suppress("UNCHECKED_CAST")
    private fun getMigrationFormat(element: Element): String {
        return ArrayList<String>().apply {
            element.annotationMirrors.forEach {
                it.elementValues.forEach {
                    val key = it.key.simpleName.toString()
                    val value = it.value.value

                    if (key == "migrations") {
                        val typeMirrors = value as List<AnnotationValue>
                        typeMirrors.forEach {
                            val declaredType = it.value as DeclaredType
                            val objectClass = declaredType.asElement()
                            add("$objectClass.class.newInstance()")
                        }
                    }
                }
            }
        }.joinToString()
    }

    private fun getDestructiveMigration(element: Element): String {
        return ArrayList<String>().apply {
            var foundDestructiveMigration = false
            val destructiveVersionsFormat: ArrayList<String> = ArrayList()

            DatabaseMapHolder.migrationAnnotationMap[element.simpleName.toString()]?.forEach {
                if (!foundDestructiveMigration) {
                    val migrationType = it.getAnnotation(Migration::class.java).migrationType
                    if (migrationType == MigrationType.DESTRUCTIVE) {
                        foundDestructiveMigration = true
                        add("builder.fallbackToDestructiveMigration();\n")
                    }
                }

                val destructiveVersions = it.getAnnotation(Migration::class.java).destructiveFrom
                destructiveVersionsFormat.add(destructiveVersions.joinToString { version ->
                    version.toString()
                })
            }

            if (destructiveVersionsFormat.isNotEmpty()) {
                val fullFormat = destructiveVersionsFormat.joinToString()
                add("builder.fallbackToDestructiveMigrationFrom($fullFormat);\n")
            }
        }.joinToString(separator = "")
    }
}