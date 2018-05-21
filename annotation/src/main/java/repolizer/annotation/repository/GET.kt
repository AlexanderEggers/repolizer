package repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class GET(val url: String,
                     val getAsList: Boolean = true,
                     val sql: String = "",
                     val maxCacheTime: Long = Long.MAX_VALUE,
                     val maxFreshTime: Long = Long.MAX_VALUE,
                     val requiresLogin: Boolean = false,
                     val showProgress: Boolean = false)