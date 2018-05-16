package repolizer.repository.database

class DatabaseBuilder<Entity> {

    var databaseLayer: DatabaseLayer? = null

    fun build(): DatabaseResource<Entity> {
        return DatabaseResource(this)
    }
}