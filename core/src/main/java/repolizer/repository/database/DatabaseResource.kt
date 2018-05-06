package repolizer.repository.database

class DatabaseResource<Entity> internal constructor(builder: DatabaseBuilder<Entity>) {

    private val databaseLayer: DatabaseLayer<Entity>? = builder.databaseLayer
    private val raw: Entity? = builder.raw

    fun execute() {
        databaseLayer?.updateDB(raw)
    }
}