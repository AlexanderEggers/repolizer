package repolizer.repository.future.worker

import kotlinx.coroutines.CoroutineDispatcher

interface FutureTaskDoAfter {
    fun doWork(runnable: () -> Unit): FutureTaskDoWork
    fun doAfter(runnable: () -> Unit): FutureTaskDoAfter
    fun withAfterWorkThread(executor: CoroutineDispatcher): FutureTaskExecute
    fun execute()
}