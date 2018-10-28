package repolizer.repository.future

import repolizer.Repolizer
import repolizer.repository.network.ExecutionType

abstract class Future<Body>(private val repolizer: Repolizer) {

    private val defaultFutureCallback = object : FutureCallback<Body> {

        override fun onFinished(body: Body?) {
            //do nothing
        }
    }

    abstract fun <Wrapper> create(): Wrapper

    @JvmOverloads
    fun executeAsync(callback: FutureCallback<Body> = defaultFutureCallback) {
        repolizer.workerThread.execute {
            val result = execute()

            repolizer.defaultMainThread.execute {
                callback.onFinished(result)
            }
        }
    }

    abstract fun execute(): Body?

    protected abstract fun onExecute(executionType: ExecutionType): Body?

    protected abstract fun onDetermineExecutionType(): ExecutionType

    protected open fun onStart() {
        //do nothing
    }

    protected open fun onFinished(result: Body?) {
        //do nothing
    }
}