package repolizer.repository.future

import repolizer.repository.util.RepositoryExecutor

object FutureBatchExecutor {

    private val executor = RepositoryExecutor
    private val defaultCallback = object : FutureCallback<Boolean> {

        override fun onFinished(body: Boolean?) {
            //do nothing
        }
    }

    @JvmOverloads
    @JvmStatic
    fun executeFutures(callback: FutureCallback<Boolean> = defaultCallback, vararg futures: Future<*>) {
        executor.workerThread.execute {
            futures.forEach {
                it.execute()
            }
            callback.onFinished(true)
        }
    }
}