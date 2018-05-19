package repolizer.database

import android.arch.persistence.room.RoomDatabase
import android.content.Context
import repolizer.repository.util.Utils.Companion.getGeneratedDatabaseProviderName

object GlobalDatabaseProvider {

    private val databaseSingletonMap: HashMap<String, RepolizerDatabase> = HashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T : RoomDatabase> getDatabase(context: Context, databaseClass: Class<*>): T {
        return if (databaseSingletonMap.containsKey(databaseClass.simpleName)) {
            (databaseSingletonMap[databaseClass.simpleName] as T?)!!
        } else {
            val databaseProvider: DatabaseProvider = Class
                    .forName(databaseClass.`package`.name
                            + getGeneratedDatabaseProviderName(databaseClass))
                    .newInstance() as DatabaseProvider
            val repolizerDatabase = databaseProvider.getDatabase(context)
            databaseSingletonMap[databaseClass.simpleName] = repolizerDatabase
            repolizerDatabase as T
        }
    }
}