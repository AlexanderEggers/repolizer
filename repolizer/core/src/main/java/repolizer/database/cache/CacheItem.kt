package repolizer.database.cache

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "cache_table")
class CacheItem(

        @PrimaryKey
        val url: String,

        @ColumnInfo(name = "cache_time")
        var cacheTime: Long = System.currentTimeMillis())