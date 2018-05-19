package repolizer.database.cache

import android.arch.persistence.room.*

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg cacheItem: CacheItem)

    @Delete
    fun delete(vararg cacheItem: CacheItem)

    @Query("DELETE FROM cache_table WHERE url IN :url")
    fun delete(vararg url: String)

    @Query("DELETE FROM cache_table")
    fun deleteAll()
}