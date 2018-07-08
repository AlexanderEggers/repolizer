package repolizer.repository.future

import repolizer.repository.network.ExecutionType

abstract class Future<B> {

    protected abstract fun onExecute(executionType: ExecutionType): B?

    protected abstract fun onDetermineExecutionType(): ExecutionType

    protected open fun onCreate() {

    }

    protected open fun onStart() {

    }

    protected open fun onFinished() {

    }
}