package repolizer.database

import repolizer.database.cache.CacheDao

abstract class RepolizerDatabase {
    abstract fun getCacheDao(): CacheDao
}