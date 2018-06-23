package repolizer.room

import android.arch.persistence.room.RoomDatabase
import repolizer.room.cache.CacheDao

abstract class RepolizerDatabase : RoomDatabase() {
    abstract fun getCacheDao(): CacheDao
}