package repolizer.repository.future

import repolizer.repository.util.RepositoryExecutor
import java.util.concurrent.Executor

object FutureHelper {

    private val executor = RepositoryExecutor

    @JvmOverloads
    @JvmStatic
    fun executeFutures(callback: FutureWorkerCallback, afterExecuteThread: Executor = executor.applicationThread) {
        executor.workerThread.execute {
            callback.onExecute()

            afterExecuteThread.execute {
                callback.onFinished()
            }
        }
    }
}