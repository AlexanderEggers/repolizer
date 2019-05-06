package repolizer.annotation.repository

import repolizer.annotation.repository.util.DataOperation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DATA(val operation: DataOperation,
                      val statement: String = "")