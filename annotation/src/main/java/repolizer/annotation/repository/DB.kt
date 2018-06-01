package repolizer.annotation.repository

import repolizer.annotation.repository.util.DatabaseOperation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DB(val databaseOperation: DatabaseOperation, val sql: String)