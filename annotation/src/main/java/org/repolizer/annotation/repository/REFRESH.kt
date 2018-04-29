package org.repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class REFRESH(val url: String,
                         val requiresLogin: Boolean = false)