package repolizer.repository.provider

import repolizer.Repolizer
import repolizer.repository.BaseRepository
import repolizer.repository.util.Utils

object GlobalRepositoryProvider {

    private val repositorySingletonMap: HashMap<String, BaseRepository<*>> = HashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseRepository<*>> getRepository(repolizer: Repolizer, repositoryClass: Class<T>): T {
        return if (repositorySingletonMap.containsKey(repositoryClass.simpleName)) {
            repositorySingletonMap[repositoryClass.simpleName] as T
        } else {
            val realRepositoryClass = Class.forName(repositoryClass.`package`.name
                    + Utils.getGeneratedRepositoryName(repositoryClass))
            val repository = realRepositoryClass.getConstructor(Repolizer::class.java).newInstance(repolizer) as T
            repositorySingletonMap[repositoryClass.simpleName] = repository
            repository
        }
    }
}