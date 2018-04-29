package org.repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PUT(val url: String,
                     val sql: String = "",
                     val requiresLogin: Boolean = false)