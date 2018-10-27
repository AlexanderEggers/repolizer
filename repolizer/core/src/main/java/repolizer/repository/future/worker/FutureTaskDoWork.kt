package repolizer.repository.future.worker

import java.util.concurrent.Executor

interface FutureTaskDoWork {
    fun doWork(runnable: () -> Unit): FutureTaskDoWork
    fun doAfter(runnable: () -> Unit): FutureTaskDoAfter
    fun withWorkerThread(executor: Executor): FutureTaskDoAfter
    fun withWorkerThread(threadName: String): FutureTaskDoAfter
    fun execute()
}