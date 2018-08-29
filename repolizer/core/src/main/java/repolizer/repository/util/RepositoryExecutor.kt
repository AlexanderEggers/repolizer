package repolizer.repository.util

import java.util.concurrent.Executor
import java.util.concurrent.Executors

object RepositoryExecutor {
    val workerThread: Executor = Executors.newSingleThreadExecutor()
    val applicationThread: Executor = Executors.newSingleThreadExecutor()
}