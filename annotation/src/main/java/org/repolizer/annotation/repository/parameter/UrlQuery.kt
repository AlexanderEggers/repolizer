package org.repolizer.annotation.repository.parameter

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class UrlQuery(val key: String)