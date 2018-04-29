package org.repolizer.annotation.database

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Migration(val type: MigrationType,
                           val migrations: Array<KClass<*>>,
                           val destructiveFrom: Int)