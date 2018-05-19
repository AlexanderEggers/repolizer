package repolizer.database

import android.arch.persistence.room.RoomDatabase
import repolizer.database.cache.CacheDao

abstract class RepolizerDatabase: RoomDatabase() {
    abstract fun getCacheDao(): CacheDao
}