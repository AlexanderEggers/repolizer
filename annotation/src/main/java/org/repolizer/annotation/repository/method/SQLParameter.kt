package org.repolizer.annotation.repository.method

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SQLParameter(val key: String)