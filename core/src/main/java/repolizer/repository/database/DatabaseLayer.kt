package repolizer.repository.database

interface DatabaseLayer<Entity> {
    fun updateDB(data: Entity?)
}