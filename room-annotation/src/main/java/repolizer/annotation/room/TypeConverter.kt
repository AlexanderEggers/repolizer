package repolizer.annotation.room

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class TypeConverter(val typeConverter: Array<KClass<*>>)