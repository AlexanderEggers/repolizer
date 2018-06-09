package repolizer.database.provider

import android.arch.persistence.room.RoomDatabase
import android.content.Context
import repolizer.annotation.database.Database
import repolizer.repository.util.Utils.Companion.getGeneratedDatabaseProviderName

object GlobalDatabaseProvider {

    private val databaseSingletonMap: HashMap<String, RoomDatabase> = HashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T> getDatabase(context: Context, databaseClass: Class<*>): T? {
        return when {
            databaseSingletonMap.containsKey(databaseClass.simpleName) -> databaseSingletonMap[databaseClass.simpleName] as? T
            databaseClass.isAnnotationPresent(Database::class.java) -> {
                return databaseClass.`package`.name
                        .let { "$it.${getGeneratedDatabaseProviderName(databaseClass)}" }
                        .let { Class.forName(it) }
                        ?.run { newInstance() as? DatabaseProvider }
                        ?.getDatabase(context)
                        ?.also { databaseSingletonMap[databaseClass.simpleName] = it } as? T
            }
            else -> null
        }
    }
}