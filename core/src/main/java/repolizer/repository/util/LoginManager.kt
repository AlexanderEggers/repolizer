package repolizer.repository.util

import android.arch.lifecycle.LiveData
import android.content.Context
import android.support.annotation.MainThread

interface LoginManager {

    fun isCurrentLoginValid(): LiveData<Boolean>

    @MainThread
    fun onLoginInvalid(appContext: Context)
}