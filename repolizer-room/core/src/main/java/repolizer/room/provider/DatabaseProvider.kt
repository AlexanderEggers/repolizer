package repolizer.room.provider

import android.arch.persistence.room.RoomDatabase
import android.content.Context

interface DatabaseProvider {
    fun getDatabase(context: Context): RoomDatabase
}