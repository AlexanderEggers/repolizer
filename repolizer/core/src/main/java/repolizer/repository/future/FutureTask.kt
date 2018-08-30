package repolizer.repository.future

import repolizer.repository.util.RepositoryExecutor
import java.util.concurrent.Executor

open class FutureTask
@JvmOverloads constructor(private val afterExecuteThread: Executor = RepositoryExecutor.applicationThread) {

    private val executor = RepositoryExecutor

    @JvmOverloads
    open fun execute(callback: FutureTaskCallback, thread: Executor = afterExecuteThread) {
        executor.workerThread.execute {
            callback.onExecute()

            thread.execute {
                callback.onFinished()
            }
        }
    }
}