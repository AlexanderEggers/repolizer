package repolizer.repository.retrofit

import android.arch.lifecycle.LiveData
import repolizer.repository.response.NetworkResponse
import repolizer.repository.request.RequestProvider
import repolizer.repository.util.AppExecutor
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataCallAdapterFactory(private val requestProvider: RequestProvider?) : CallAdapter.Factory() {

    private val appExecutor: AppExecutor = AppExecutor

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (CallAdapter.Factory.getRawType(returnType) != LiveData::class.java) {
            throw IllegalArgumentException("Type must have the raw type of LiveData.")
        }

        val observableType = CallAdapter.Factory.getParameterUpperBound(0, returnType as ParameterizedType)
        val rawObservableType = CallAdapter.Factory.getRawType(observableType)

        if (rawObservableType != NetworkResponse::class.java) {
            throw IllegalArgumentException("Type must be a resource.")
        }

        if (observableType !is ParameterizedType) {
            throw IllegalArgumentException("Resource must be parameterized.")
        }

        val bodyType = CallAdapter.Factory.getParameterUpperBound(0, observableType)
        return LiveDataCallAdapter(bodyType, requestProvider, appExecutor)
    }
}
