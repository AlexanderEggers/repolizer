package repolizer.annotation.database

import repolizer.annotation.database.util.BaseMigration
import repolizer.annotation.database.util.MigrationType
import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Migration(val type: MigrationType,
                           val migrations: Array<KClass<BaseMigration>>,
                           val destructiveFrom: IntArray = [])