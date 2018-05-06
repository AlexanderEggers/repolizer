package repolizer.repository.database

class DatabaseBuilder<Entity> {

    val raw: Entity? = null
    val databaseLayer: DatabaseLayer<Entity>? = null

    fun build(): DatabaseResource<Entity> {
        return DatabaseResource(this)
    }
}