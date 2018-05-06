package repolizer.repository.network

interface NetworkLayer<Entity> {
    fun updateDB(entity: Entity)
}
