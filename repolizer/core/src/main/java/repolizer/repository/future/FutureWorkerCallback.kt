package repolizer.repository.future

interface FutureWorkerCallback {
    fun onExecute()
    fun onFinished()
}