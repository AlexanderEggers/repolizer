package repolizer.repository.response

import repolizer.repository.util.RequestType

abstract class ProgressController constructor(private val requestProvider: RequestProvider?) {

    fun show(url: String, requestType: RequestType) {
        onShow(url, requestType)
    }

    fun close() {
        onClose()
    }

    fun cancel() {
        onCancel(requestProvider)
    }

    protected abstract fun onShow(url: String, requestType: RequestType)
    protected abstract fun onClose()
    protected abstract fun onCancel(requestProvider: RequestProvider?)
}