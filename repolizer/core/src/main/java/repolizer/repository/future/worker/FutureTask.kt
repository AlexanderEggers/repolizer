package repolizer.repository.future.worker

import repolizer.repository.util.RepositoryExecutor
import java.util.concurrent.Executor

open class FutureTask
constructor(private var workerThread: Executor,
            private var afterExecuteThread: Executor) : FutureTaskDoWork, FutureTaskDoAfter, FutureTaskExecute {

    private val workerExecutorMap = HashMap<String, Executor?>()
    private val workerBlockMap = HashMap<String, () -> Unit>()
    private var postWorkerBlock: () -> Unit? = { }

    override fun doWork(runnable: () -> Unit): FutureTaskDoWork {
        workerBlockMap["worker${workerBlockMap.size}"] = runnable
        return this
    }

    override fun withWorkerThread(executor: Executor): FutureTaskDoAfter {
        workerExecutorMap["worker${workerBlockMap.size - 1}"] = executor
        return this
    }

    override fun withWorkerThread(threadName: String): FutureTaskDoAfter {
        workerExecutorMap["worker${workerBlockMap.size - 1}"] = RepositoryExecutor.getRepositoryThread(threadName)
        return this
    }

    override fun doAfter(runnable: () -> Unit): FutureTaskDoAfter {
        postWorkerBlock = runnable
        return this
    }

    override fun withAfterWorkThread(executor: Executor): FutureTaskExecute {
        afterExecuteThread = executor
        return this
    }

    override fun execute() {
        if (workerBlockMap.isNotEmpty()) {
            executeWorkerTask(0)
        } else {
            throw IllegalStateException("FutureTask requires at least one worker task.")
        }
    }

    private fun executeWorkerTask(position: Int) {
        val executor = workerExecutorMap["worker$position"] ?: workerThread
        executor.execute {
            workerBlockMap["worker$position"]?.invoke()

            if (workerBlockMap.size > position + 1) {
                executeWorkerTask(position + 1)
            } else {
                executeAfterExecuteTask()
            }
        }
    }

    private fun executeAfterExecuteTask() {
        afterExecuteThread.execute {
            postWorkerBlock()
        }
    }
}