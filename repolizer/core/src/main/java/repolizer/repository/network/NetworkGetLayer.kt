package repolizer.repository.network

import android.arch.lifecycle.LiveData
import repolizer.database.cache.CacheState

interface NetworkGetLayer<Entity> : NetworkLayer<Entity> {
    fun needsFetchByTime(fullUrlId: String): LiveData<CacheState>
    fun updateFetchTime(fullUrlId: String)

    fun removeAllData()
    fun getData(): LiveData<Entity>
    fun updateDB(entity: Entity)
}