package repolizer.repository.future.worker

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import repolizer.repository.util.RepositoryExecutor

open class FutureTask
constructor(private var workerThread: CoroutineDispatcher,
            private var afterExecuteThread: CoroutineDispatcher) : FutureTaskDoWork, FutureTaskDoAfter, FutureTaskExecute {

    private val workerExecutorMap = HashMap<String, CoroutineDispatcher?>()
    private val workerBlockMap = HashMap<String, () -> Unit>()
    private var postWorkerBlock: () -> Unit? = { }

    override fun doWork(runnable: () -> Unit): FutureTaskDoWork {
        workerBlockMap["worker${workerBlockMap.size}"] = runnable
        return this
    }

    override fun withWorkerThread(executor: CoroutineDispatcher): FutureTaskDoAfter {
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

    override fun withAfterWorkThread(executor: CoroutineDispatcher): FutureTaskExecute {
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

        GlobalScope.launch(executor) {
            workerBlockMap["worker$position"]?.invoke()

            if (workerBlockMap.size > position + 1) {
                executeWorkerTask(position + 1)
            } else {
                executeAfterExecuteTask()
            }
        }
    }

    private fun executeAfterExecuteTask() {
        GlobalScope.launch(afterExecuteThread) {
            postWorkerBlock()
        }
    }
}