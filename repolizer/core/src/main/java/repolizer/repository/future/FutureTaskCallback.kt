package repolizer.repository.future

interface FutureTaskCallback {
    fun onExecute()
    fun onFinished()
}