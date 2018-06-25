package repolizer.repository.login

interface LoginManager {
    fun isCurrentLoginValid(): Boolean
    fun onLoginInvalid()
}