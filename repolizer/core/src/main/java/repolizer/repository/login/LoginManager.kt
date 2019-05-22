package repolizer.repository.login

import repolizer.repository.network.NetworkFutureRequest

interface LoginManager {
    fun isCurrentLoginValid(networkFutureRequest: NetworkFutureRequest): Boolean
    fun onLoginInvalid()
}