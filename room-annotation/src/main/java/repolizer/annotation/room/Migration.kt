package repolizer.annotation.room

import repolizer.annotation.room.util.MigrationType
import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Migration(val migrationType: MigrationType,
                           val migrations: Array<KClass<*>> = [],
                           val destructiveFrom: IntArray = [])