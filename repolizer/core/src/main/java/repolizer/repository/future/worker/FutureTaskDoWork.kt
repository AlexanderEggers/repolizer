package repolizer.repository.future.worker

import kotlinx.coroutines.CoroutineDispatcher

interface FutureTaskDoWork {
    fun doWork(runnable: () -> Unit): FutureTaskDoWork
    fun doAfter(runnable: () -> Unit): FutureTaskDoAfter
    fun withWorkerThread(executor: CoroutineDispatcher): FutureTaskDoAfter
    fun withWorkerThread(threadName: String): FutureTaskDoAfter
    fun execute()
}