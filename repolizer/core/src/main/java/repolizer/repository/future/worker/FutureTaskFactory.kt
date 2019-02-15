package repolizer.repository.future.worker

import kotlinx.coroutines.CoroutineDispatcher
import repolizer.repository.util.RepositoryExecutor

open class FutureTaskFactory
@JvmOverloads constructor(private val defaultWorkerThread: CoroutineDispatcher = RepositoryExecutor.getRepositoryDefaultThread(),
                          private val defaultAfterExecuteThread: CoroutineDispatcher = RepositoryExecutor.applicationThread) {

    fun create(): FutureTaskDoWork {
        return FutureTask(defaultWorkerThread, defaultAfterExecuteThread)
    }
}