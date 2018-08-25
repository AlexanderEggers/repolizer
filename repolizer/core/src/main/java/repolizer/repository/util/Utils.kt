package repolizer.repository.util

class Utils {

    companion object {

        @JvmStatic
        fun getGeneratedRepositoryName(repositoryClass: Class<*>): String {
            return "Generated_${repositoryClass.simpleName}"
        }
    }
}