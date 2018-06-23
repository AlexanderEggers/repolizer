package repolizer.repository.database

class DatabaseBuilder {

    var databaseLayer: DatabaseLayer? = null

    fun build(): DatabaseResource {
        return DatabaseResource(this)
    }
}