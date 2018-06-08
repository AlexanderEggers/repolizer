package repolizer.repository.provider

import repolizer.Repolizer
import repolizer.annotation.repository.Repository
import repolizer.repository.BaseRepository
import repolizer.repository.util.Utils.Companion.getGeneratedRepositoryName

object GlobalRepositoryProvider {

    private val repositorySingletonMap: HashMap<String, BaseRepository<*>> = HashMap()

    @Suppress("UNCHECKED_CAST")
    fun getRepository(repolizer: Repolizer, repositoryClass: Class<*>): BaseRepository<*>? {
        return when {
            repositorySingletonMap.containsKey(repositoryClass.simpleName) -> repositorySingletonMap[repositoryClass.simpleName]
            repositoryClass.isAnnotationPresent(Repository::class.java) -> {
                val repository: BaseRepository<*>? = repositoryClass
                        .let { repositoryClass.`package`.name }
                        .let { "$it.${getGeneratedRepositoryName(repositoryClass)}" }
                        .let { Class.forName(it) }.getConstructor(Repolizer::class.java)
                        .run { newInstance(repolizer) as BaseRepository<*> }
                return repository?.also {
                    repositorySingletonMap[repositoryClass.simpleName] = it
                }
            }
            else -> null
        }
    }
}