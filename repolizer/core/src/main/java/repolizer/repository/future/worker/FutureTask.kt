package repolizer.repository.future.worker

import repolizer.repository.util.RepositoryExecutor
import java.util.concurrent.Executor

open class FutureTask
constructor(private var workerThread: Executor,
            private var afterExecuteThread: Executor) : FutureTaskDoWork, FutureTaskDoAfter {

    private val workerExecutorMap = HashMap<String, Executor?>()
    private val workerBlockMap = HashMap<String, () -> Unit>()
    private var postWorkerBlock: () -> Unit? = { }

    override fun doWork(runnable: () -> Unit): FutureTaskDoWork {
        workerBlockMap["worker${workerBlockMap.size}"] = runnable
        return this
    }

    override fun withWorkerThread(executor: Executor): FutureTaskDoAfter {
        workerExecutorMap["worker${workerBlockMap.size}"] = executor
        return this
    }

    override fun withWorkerThread(threadName: String): FutureTaskDoAfter {
        workerExecutorMap["worker${workerBlockMap.size}"] = RepositoryExecutor.getRepositoryThread(threadName)
        return this
    }

    override fun doAfter(runnable: () -> Unit): FutureTaskDoAfter {
        postWorkerBlock = runnable
        return this
    }

    override fun withAfterWorkThread(executor: Executor): FutureTaskDoAfter {
        afterExecuteThread = executor
        return this
    }

    override fun execute() {
        workerBlockMap.forEach {
            val executor = workerExecutorMap[it.key] ?: workerThread
            executor.execute {
                it.value
            }
        }

        afterExecuteThread.execute {
            postWorkerBlock()
        }
    }
}