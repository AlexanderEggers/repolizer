package repolizer.repository.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object RepositoryExecutor {
    private val repositoryThreadMap = HashMap<String, CoroutineDispatcher>()
    private val defaultWorkerThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    val applicationThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun addRepositoryThread(name: String) {
        repositoryThreadMap[name] = defaultWorkerThread
    }

    fun getRepositoryThread(name: String): CoroutineDispatcher? {
        return repositoryThreadMap[name]
    }

    fun getRepositoryDefaultThread(): CoroutineDispatcher {
        return defaultWorkerThread
    }
}