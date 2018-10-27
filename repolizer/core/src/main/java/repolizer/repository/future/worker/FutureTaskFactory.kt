package repolizer.repository.future.worker

import repolizer.repository.util.RepositoryExecutor
import java.util.concurrent.Executor

open class FutureTaskFactory
@JvmOverloads constructor(private val defaultWorkerThread: Executor = RepositoryExecutor.workerThread,
                          private val defaultAfterExecuteThread: Executor = RepositoryExecutor.applicationThread) {

    fun create(): FutureTaskDoWork {
        return FutureTask(defaultWorkerThread, defaultAfterExecuteThread)
    }
}