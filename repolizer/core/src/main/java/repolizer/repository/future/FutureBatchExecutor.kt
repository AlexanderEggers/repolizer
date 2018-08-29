package repolizer.repository.future

import repolizer.repository.util.RepositoryExecutor

object FutureBatchExecutor {

    private val executor = RepositoryExecutor
    private val defaultCallback = object : FutureBatchCallback {

        override fun onFinished() {
            //do nothing
        }
    }

    @JvmOverloads
    @JvmStatic
    fun executeFutures(callback: FutureBatchCallback = defaultCallback, vararg futures: Future<*>) {
        executor.workerThread.execute {
            futures.forEach {
                it.execute()
            }
            callback.onFinished()
        }
    }
}