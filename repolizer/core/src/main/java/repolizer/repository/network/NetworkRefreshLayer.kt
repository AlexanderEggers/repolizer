package repolizer.repository.network

interface NetworkRefreshLayer<Entity> : NetworkLayer<Entity> {
    fun updateFetchTime(fullUrlId: String)
    fun updateDB(entity: Entity)
}
