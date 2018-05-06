package repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class GET(val url: String,
                     val sql: String = "",
                     val requiresLogin: Boolean = false,
                     val showProgress: Boolean = false)