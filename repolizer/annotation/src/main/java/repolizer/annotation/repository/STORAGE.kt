package repolizer.annotation.repository

import repolizer.annotation.repository.util.StorageOperation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class STORAGE(val storageOperation: StorageOperation,
                         val sql: String = "")