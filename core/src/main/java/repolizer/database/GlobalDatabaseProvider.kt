package repolizer.database

import android.arch.persistence.room.RoomDatabase
import android.content.Context
import repolizer.repository.util.Utils.Companion.getGeneratedDatabaseProviderName

object GlobalDatabaseProvider {

    private val databaseSingletonMap: HashMap<String, RoomDatabase> = HashMap()

    fun getDatabase(context: Context, databaseClass: Class<*>): RoomDatabase {
        return if (databaseSingletonMap.containsKey(databaseClass.simpleName)) {
            databaseSingletonMap[databaseClass.simpleName]!!
        } else {
            val databaseProvider: DatabaseProvider = Class
                    .forName(databaseClass.`package`.name
                            + getGeneratedDatabaseProviderName(databaseClass))
                    .newInstance() as DatabaseProvider
            val roomDatabase = databaseProvider.getDatabase(context)
            databaseSingletonMap[databaseClass.simpleName] = roomDatabase
            roomDatabase
        }
    }
}