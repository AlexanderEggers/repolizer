package repolizer.annotation.repository

import repolizer.annotation.repository.util.StorageOperation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class STORAGE(val operation: StorageOperation,
                         val sql: String = "")