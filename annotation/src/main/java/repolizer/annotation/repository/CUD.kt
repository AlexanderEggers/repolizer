package repolizer.annotation.repository

import repolizer.annotation.repository.util.CudType

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CUD(val url: String,
                     val cudType: CudType,
                     val requiresLogin: Boolean = false,
                     val showProgress: Boolean = false)