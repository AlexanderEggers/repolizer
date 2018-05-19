package repolizer.repository.util

import android.arch.lifecycle.LiveData
import android.content.Context
import android.support.annotation.WorkerThread

interface LoginManager {

    @WorkerThread
    fun isCurrentLoginValid(): LiveData<Boolean>

    fun onLoginInvalid(context: Context)
}