package repolizer.repository.util

class Utils {

    companion object {

        fun getGeneratedRepositoryName(repositoryClass: Class<*>): String {
            return "Generated_${repositoryClass.simpleName}"
        }
    }
}