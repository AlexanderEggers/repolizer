package repolizer.repository.network

import android.arch.lifecycle.LiveData

interface NetworkGetLayer<Entity> : NetworkLayer<Entity> {
    fun needsFetchByTime(fullUrlId: String): Boolean
    fun updateFetchTime(fullUrlId: String)

    fun getData(): LiveData<Entity>
}