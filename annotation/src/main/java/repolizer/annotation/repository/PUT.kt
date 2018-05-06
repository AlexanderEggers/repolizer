package repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PUT(val url: String,
                     val updateDB: Boolean = false,
                     val requiresLogin: Boolean = false,
                     val showProgress: Boolean = false)