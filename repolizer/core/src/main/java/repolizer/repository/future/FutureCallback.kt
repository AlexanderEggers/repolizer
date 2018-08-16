package repolizer.repository.future

interface FutureCallback<Body> {
    fun onFinished(body: Body?)
}