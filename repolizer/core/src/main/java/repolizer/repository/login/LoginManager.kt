package repolizer.repository.login

import android.arch.lifecycle.LiveData
import android.content.Context
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread

interface LoginManager {

    @WorkerThread
    fun isCurrentLoginValid(): LiveData<Boolean>

    @MainThread
    fun onLoginInvalid(appContext: Context)
}