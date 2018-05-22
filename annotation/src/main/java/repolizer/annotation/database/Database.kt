package repolizer.annotation.database

import repolizer.annotation.database.util.DatabaseType
import repolizer.annotation.database.util.JournalMode

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Database(val name: String,
                          val version: Int,
                          val type: DatabaseType,
                          val exportSchema: Boolean = true,
                          val journalMode: JournalMode = JournalMode.AUTOMATIC)