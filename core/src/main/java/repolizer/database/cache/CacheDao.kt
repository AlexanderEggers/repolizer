package repolizer.database.cache

import android.arch.persistence.room.*

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cacheItem: CacheItem)

    @Query("DELETE FROM cache_table")
    fun deleteAll()

    @Query("DELETE FROM cache_table WHERE url = :url")
    fun deleteCacheUrl(url: String)
}