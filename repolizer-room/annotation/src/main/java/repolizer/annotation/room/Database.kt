package repolizer.annotation.room

import repolizer.annotation.room.util.DatabaseType
import repolizer.annotation.room.util.JournalMode

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Database(val name: String = "database",
                          val version: Int,
                          val type: DatabaseType,
                          val exportSchema: Boolean = true,
                          val journalMode: JournalMode = JournalMode.AUTOMATIC)