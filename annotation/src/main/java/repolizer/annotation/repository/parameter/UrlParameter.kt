package repolizer.annotation.repository.parameter

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class UrlParameter(val key: String)