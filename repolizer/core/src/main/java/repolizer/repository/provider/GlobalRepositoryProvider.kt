package repolizer.repository.provider

import repolizer.Repolizer
import repolizer.annotation.repository.Repository
import repolizer.repository.BaseRepository
import repolizer.repository.util.Utils.Companion.getGeneratedRepositoryName

object GlobalRepositoryProvider {

    private val repositorySingletonMap: HashMap<String, BaseRepository> = HashMap()

    fun getRepository(repolizer: Repolizer, repositoryClass: Class<*>): BaseRepository? {
        return when {
            repositorySingletonMap.containsKey(repositoryClass.simpleName) -> repositorySingletonMap[repositoryClass.simpleName]
            repositoryClass.isAnnotationPresent(Repository::class.java) -> {
                return repositoryClass.`package`.name
                        .let { "$it.${getGeneratedRepositoryName(repositoryClass)}" }
                        .let { Class.forName(it) }
                        ?.getConstructor(Repolizer::class.java)
                        ?.let { it.newInstance(repolizer) as? BaseRepository }
                        ?.also { repositorySingletonMap[repositoryClass.simpleName] = it }
            }
            else -> throw IllegalStateException("Internal error: Your used class for the " +
                    "function Repolizer.getRepository(Class<*>) is missing the @Repository " +
                    "annotation.")
        }
    }
}