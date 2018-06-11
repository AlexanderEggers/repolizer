package repolizer.repository.progress

import android.util.Log
import repolizer.repository.request.RequestProvider
import java.util.*
import kotlin.collections.HashMap

abstract class ProgressController constructor(private val requestProvider: RequestProvider?) {

    private val sourceMap: HashMap<String, Int> = HashMap()

    private val queryProgressParams: LinkedList<ProgressData> = LinkedList()
    private val queryUrl: LinkedList<String> = LinkedList()

    private val syncLock = Any()

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

    internal fun internalShow(url: String, progressData: ProgressData?) {
        synchronized(syncLock) {
            increaseSourceCount(url)

            if (isShowingProgress) {
                isShowingProgress = true
                onShow(url, progressData)
            } else {
                queryProgressParams.addFirst(progressData)
                queryUrl.addFirst(url)
            }
        }
    }

    internal fun internalClose(url: String) {
        synchronized(syncLock) {
            decreaseSourceCount(url)
            if (sourceMap.isEmpty()) {
                isShowingProgress = false
                onClose()
            } else {
                onShow(queryUrl.pollLast(), queryProgressParams.pollLast())
            }
        }
    }

    protected abstract fun onShow(url: String, progressData: ProgressData?)
    protected abstract fun onClose()

    fun cancel() {
        synchronized(syncLock) {
            sourceMap.clear()
            requestProvider?.cancelAllRequests() ?: Log.w(ProgressController::class.java.name,
                    "RequestProvider is null. This progress layer will be closed but nothing " +
                            "else will happen. Not taking care of the running calls, could " +
                            "confuse your users and damage the reputation of your app.")
            onClose()
        }
    }

    fun resetController() {
        synchronized(syncLock) {
            sourceMap.clear()
            queryProgressParams.clear()
            queryUrl.clear()
            isShowingProgress = false
            onClose()
        }
    }
}