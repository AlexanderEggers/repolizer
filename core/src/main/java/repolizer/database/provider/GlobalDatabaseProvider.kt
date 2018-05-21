package repolizer.database.provider

import android.arch.persistence.room.RoomDatabase
import android.content.Context
import repolizer.repository.util.Utils.Companion.getGeneratedDatabaseProviderName

object GlobalDatabaseProvider {

    private val databaseSingletonMap: HashMap<String, RoomDatabase> = HashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T> getDatabase(context: Context, databaseClass: Class<*>): T {
        return if (databaseSingletonMap.containsKey(databaseClass.simpleName)) {
            databaseSingletonMap[databaseClass.simpleName]!! as T
        } else {
            val databaseProvider: DatabaseProvider = Class
                    .forName(databaseClass.`package`.name +
                            ".${getGeneratedDatabaseProviderName(databaseClass)}")
                    .newInstance() as DatabaseProvider
            val roomDatabase = databaseProvider.getDatabase(context)
            databaseSingletonMap[databaseClass.simpleName] = roomDatabase
            roomDatabase as T
        }
    }
}