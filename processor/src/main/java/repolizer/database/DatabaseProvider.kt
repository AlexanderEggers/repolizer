package repolizer.database

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.database.Database
import repolizer.annotation.database.Migration
import repolizer.annotation.database.util.DatabaseType
import repolizer.annotation.database.util.JournalMode
import repolizer.annotation.database.util.MigrationType
import repolizer.util.ProcessorUtil
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

    @Suppress("UNCHECKED_CAST")
    fun build(element: Element, databaseName: String, realDatabaseClassName: ClassName): TypeSpec {
        val databaseFileName = element.getAnnotation(Database::class.java).name

        val providerBuilder = TypeSpec.classBuilder(ProcessorUtil.getGeneratedDatabaseProviderName(databaseName))
                .addSuperinterface(classDatabaseProvider)
                .addModifiers(Modifier.PUBLIC)

        val methodBuilder = MethodSpec.methodBuilder("getDatabase")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(classContext, "context")

        val databaseType = element.getAnnotation(Database::class.java).type
        if (databaseType == DatabaseType.PERSISTENT) {
            methodBuilder.addStatement("$classRoomDatabaseBuilder builder = $classRoom.databaseBuilder(context, $realDatabaseClassName.class, \"$databaseFileName\")")
        } else {
            methodBuilder.addStatement("$classRoomDatabaseBuilder builder = $classRoom.inMemoryDatabaseBuilder(context, $realDatabaseClassName.class)")
        }

        val journalMode = element.getAnnotation(Database::class.java).journalMode
        val realJournalMode = when (journalMode) {
            JournalMode.AUTOMATIC -> "$classJournalMode.AUTOMATIC"
            JournalMode.TRUNCATE -> "$classJournalMode.TRUNCATE"
            JournalMode.WRITE_AHEAD_LOGGING -> "$classJournalMode.WRITE_AHEAD_LOGGING"
        }
        methodBuilder.addStatement("builder.setJournalMode($realJournalMode)")

        var foundDestructiveMigration = false
        var destructiveVersionsFormat = ""
        DatabaseMapHolder.migrationAnnotationMap[element.simpleName.toString()]?.forEach {
            val migrationType = it.getAnnotation(Migration::class.java).migrationType
            if (migrationType == MigrationType.DESTRUCTIVE) {
                foundDestructiveMigration = true
            }

            val destructiveVersions = it.getAnnotation(Migration::class.java).destructiveFrom
            destructiveVersions.forEach {
                if (!destructiveVersionsFormat.isEmpty()) {
                    destructiveVersionsFormat += ", "
                }
                destructiveVersionsFormat += it.toString()
            }
        }

        if (foundDestructiveMigration) {
            methodBuilder.addStatement("builder.fallbackToDestructiveMigration()")
        }

        if (!destructiveVersionsFormat.isEmpty()) {
            methodBuilder.addStatement("builder.fallbackToDestructiveMigrationFrom($destructiveVersionsFormat)")
        }

        var migrationFormat = ""
        element.annotationMirrors.forEach {
            it.elementValues.forEach {
                val key = it.key.simpleName.toString()
                val value = it.value.value

                if (key == "migrations") {
                    val typeMirrors = value as List<AnnotationValue>
                    typeMirrors.forEach {
                        val declaredType = it.value as DeclaredType
                        val objectClass = declaredType.asElement()

                        if (!migrationFormat.isEmpty()) {
                            migrationFormat += ", "
                        }
                        migrationFormat += "$objectClass.class.newInstance()"
                    }
                }
            }
        }

        if (!migrationFormat.isEmpty()) {
            methodBuilder.addCode("\n")
            methodBuilder.addCode("try {\n")
            methodBuilder.addCode("     builder.addMigrations($migrationFormat);\n")
            methodBuilder.addCode("} catch(InstantiationException|IllegalAccessException e) {\n")
            methodBuilder.addCode("     e.printStackTrace();\n")
            methodBuilder.addCode("}\n\n")
        }

        methodBuilder.addStatement("return builder.build()")
                .returns(classRoomDatabase)


        return providerBuilder
                .addMethod(methodBuilder.build())
                .build()
    }
}