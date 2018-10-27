package repolizer.repository.util

import java.util.concurrent.Executor
import java.util.concurrent.Executors

object RepositoryExecutor {
    private val repositoryThreadMap = HashMap<String, Executor>()
    private val defaultWorkerThread: Executor = Executors.newSingleThreadExecutor()

    val applicationThread: Executor = Executors.newSingleThreadExecutor()

    fun addRepositoryThread(name: String) {
        repositoryThreadMap[name] = defaultWorkerThread
    }

    fun getRepositoryThread(name: String): Executor? {
        return repositoryThreadMap[name]
    }

    fun getRepositoryDefaultThread(): Executor {
        return defaultWorkerThread
    }
}