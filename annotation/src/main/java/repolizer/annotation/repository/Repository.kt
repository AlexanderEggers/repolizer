package repolizer.annotation.repository

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Repository(val entity: KClass<*>,
                            val database: KClass<*>,
                            val cacheTime: Long = Long.MAX_VALUE,
                            val freshTime: Long = Long.MAX_VALUE)