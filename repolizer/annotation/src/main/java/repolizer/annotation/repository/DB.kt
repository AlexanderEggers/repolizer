package repolizer.annotation.repository

import repolizer.annotation.repository.util.DatabaseOperation
import repolizer.annotation.repository.util.OnConflictStrategy

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DB(val databaseOperation: DatabaseOperation,
                    val sql: String = "")