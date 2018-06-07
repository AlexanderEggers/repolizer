package repolizer.annotation.database

import repolizer.annotation.database.util.MigrationType
import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Migration(val migrationType: MigrationType,
                           val migrations: Array<KClass<out Migration>> = [],
                           val destructiveFrom: IntArray = [])