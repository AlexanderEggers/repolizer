package repolizer.repository.progress

import repolizer.repository.response.RequestProvider
import java.util.*
import kotlin.collections.HashMap

abstract class ProgressController constructor(private val requestProvider: RequestProvider?) {

    private val sourceMap: HashMap<String, Int> = HashMap()

    private val queryProgressParams: LinkedList<ProgressParams> = LinkedList()
    private val queryUrl: LinkedList<String> = LinkedList()

    var isShowingProgress = false
        private set

    private fun increaseSourceCount(url: String) {
        val count = sourceMap[url] ?: 0
        sourceMap[url] = count + 1
    }

    private fun decreaseSourceCount(url: String) {
        val count = (sourceMap[url] ?: 1) - 1
        if (count == 0) sourceMap.remove(url)
        else sourceMap[url] = count
    }

    internal fun internalShow(url: String, progressParams: ProgressParams?) {
        increaseSourceCount(url)

        if (isShowingProgress) {
            isShowingProgress = true
            onShow(url, progressParams)
        } else {
            queryProgressParams.addFirst(progressParams)
            queryUrl.addFirst(url)
        }
    }

    internal fun internalClose(url: String) {
        decreaseSourceCount(url)
        if (sourceMap.isEmpty()) {
            isShowingProgress = false
            onClose()
        } else {
            onShow(queryUrl.pollLast(), queryProgressParams.pollLast())
        }
    }

    internal abstract fun onShow(url: String, progressParams: ProgressParams?)
    internal abstract fun onClose()

    fun cancel() {
        sourceMap.clear()
        requestProvider?.cancelAllRequests()
        onClose()
    }
}