package repolizer.repository.future.worker

import java.util.concurrent.Executor

interface FutureTaskDoAfter {
    fun doWork(runnable: () -> Unit): FutureTaskDoWork
    fun doAfter(runnable: () -> Unit): FutureTaskDoAfter
    fun withAfterWorkThread(executor: Executor): FutureTaskExecute
    fun execute()
}