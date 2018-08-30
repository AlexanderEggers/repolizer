package repolizer.repository.future

import repolizer.repository.util.RepositoryExecutor
import java.util.concurrent.Executor

class FutureTask
@JvmOverloads constructor(private val afterExecuteThread: Executor = RepositoryExecutor.applicationThread) {

    private val executor = RepositoryExecutor

    fun execute(callback: FutureTaskCallback) {
        executor.workerThread.execute {
            callback.onExecute()

            afterExecuteThread.execute {
                callback.onFinished()
            }
        }
    }
}