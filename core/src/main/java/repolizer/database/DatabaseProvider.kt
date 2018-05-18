package repolizer.database

import android.content.Context

interface DatabaseProvider {
    fun getDatabase(context: Context): RepolizerDatabase
}