package org.repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class REFRESH(val url: String,
                         val cacheTime: Long = Long.MAX_VALUE,
                         val freshTime: Long = Long.MAX_VALUE,
                         val requiresLogin: Boolean = false)