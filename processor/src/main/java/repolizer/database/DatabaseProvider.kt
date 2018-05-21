package repolizer.database

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import repolizer.annotation.database.Database
import repolizer.annotation.database.util.DatabaseType
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

        var migrationFormat = ""
        var destructiveVersionsFormat = ""
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
                        migrationFormat += "$objectClass.newInstance()"
                    }
                } else if (key == "destructiveFrom") {
                    val destructiveVersions = value as IntArray
                    destructiveVersions.forEach {
                        if (!destructiveVersionsFormat.isEmpty()) {
                            destructiveVersionsFormat += ", "
                        }
                        destructiveVersionsFormat += it.toString()
                    }
                }
            }
        }

        if (!migrationFormat.isEmpty()) {
            methodBuilder.addStatement("builder.addMigrations($migrationFormat)")
        }

        if (!destructiveVersionsFormat.isEmpty()) {
            methodBuilder.addStatement("builder.fallbackToDestructiveMigrationFrom($destructiveVersionsFormat)")
        }

        methodBuilder.addStatement("return builder.build()")
                .returns(classRoomDatabase)


        return providerBuilder
                .addMethod(methodBuilder.build())
                .build()
    }
}