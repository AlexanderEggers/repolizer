package org.repolizer.annotation.database

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Database(val name: String,
                          val version: Int,
                          val type: DatabaseType)