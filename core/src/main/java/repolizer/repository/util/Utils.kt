package repolizer.repository.util

class Utils {

    companion object {
        private const val CACHE_URL_FORMAT_BASE = "CID"

        fun makeUrlId(url: String?): String {
            return if (url == null || url.isEmpty())
                "0"
            else
                CACHE_URL_FORMAT_BASE + url.hashCode().toString()
        }
    }
}