package repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class GET(val url: String = "",
                     val ignoreEmptyUrl: Boolean = false,
                     val insertStatement: String = "",
                     val queryStatement: String = "",
                     val deleteStatement: String = "",
                     val maxCacheTime: Long = Long.MAX_VALUE,
                     val maxFreshTime: Long = Long.MAX_VALUE,
                     val requiresLogin: Boolean = false,
                     val saveData: Boolean = true,
                     val connectionOnly: Boolean = false)