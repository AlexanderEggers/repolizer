package repolizer.adapter.network.retrofit

class RetrofitAdapterUtils {

    companion object {

        fun prepareUrl(url: String): String {
            return url.split("?")[0]
        }
    }
}