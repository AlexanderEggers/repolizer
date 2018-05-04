package repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class POST(val url: String,
                      val updateDB: Boolean = false,
                      val requiresLogin: Boolean = false)