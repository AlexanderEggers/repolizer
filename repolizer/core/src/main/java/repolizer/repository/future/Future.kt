package repolizer.repository.future

import repolizer.repository.network.ExecutionType
import repolizer.repository.util.RepositoryExecutor

abstract class Future<Body> {

    private val executor = RepositoryExecutor
    private val defaultFutureCallback = object : FutureCallback<Body> {

        override fun onFinished(body: Body?) {
            //do nothing
        }
    }

    abstract fun <Wrapper> create(): Wrapper

    @JvmOverloads
    fun executeAsync(callback: FutureCallback<Body> = defaultFutureCallback) {
        executor.workerThread.execute {
            callback.onFinished(execute())
        }
    }

    abstract fun execute(): Body?

    protected abstract fun onExecute(executionType: ExecutionType): Body?

    protected abstract fun onDetermineExecutionType(): ExecutionType

    protected open fun onCreate() {
        //do nothing by default
    }

    protected open fun onStart() {
        //do nothing by default
    }

    protected open fun onFinished() {
        //do nothing by default
    }
}