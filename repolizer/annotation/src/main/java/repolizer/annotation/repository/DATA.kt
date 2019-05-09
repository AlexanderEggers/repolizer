package repolizer.annotation.repository

import repolizer.annotation.repository.util.DataOperation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DATA(val operation: DataOperation,
                      val operationStatement: String = "",
                      val returnStatement: String = "",
                      val overrideEmptyReturnStatement: Boolean = false)