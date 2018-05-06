package repolizer.repository.network

import com.google.gson.reflect.TypeToken
import repolizer.repository.database.DatabaseBuilder
import java.io.Serializable

class RepositoryActionBuilderFactory<Dao, Entity> {

    fun createNetworkBuilder(typeToken: TypeToken<*>): NetworkBuilder<Entity> {
        return NetworkBuilder(typeToken)
    }

    fun createNetworkBuilder(clazz: Class<out Serializable>): NetworkBuilder<Entity> {
        return NetworkBuilder(clazz)
    }

    fun createDatabaseBuilder(): DatabaseBuilder<Dao, Entity> {
        return DatabaseBuilder()
    }
}