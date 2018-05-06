package repolizer.repository.util

import android.content.Context
import android.support.annotation.WorkerThread

interface LoginManager {

    @WorkerThread
    fun isCurrentLoginValid(): Boolean

    fun onLoginInvalid(context: Context)
}