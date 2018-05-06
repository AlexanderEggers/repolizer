package repolizer.repository.util

import com.google.gson.reflect.TypeToken
import repolizer.repository.database.DatabaseBuilder
import repolizer.repository.network.NetworkBuilder
import java.io.Serializable

class RepositoryActionBuilderFactory<Entity> {

    fun createNetworkBuilder(typeToken: TypeToken<*>): NetworkBuilder<Entity> {
        return NetworkBuilder(typeToken)
    }

    fun createNetworkBuilder(clazz: Class<out Serializable>): NetworkBuilder<Entity> {
        return NetworkBuilder(clazz)
    }

    fun createDatabaseBuilder(): DatabaseBuilder<Entity> {
        return DatabaseBuilder()
    }
}