package org.demo.weatherapp.api

import android.app.AlarmManager
import androidx.lifecycle.LiveData
import org.demo.weatherapp.model.WeatherModel
import repolizer.annotation.repository.CACHE
import repolizer.annotation.repository.CUD
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.CacheBody
import repolizer.annotation.repository.parameter.UrlParameter
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.CudType
import repolizer.repository.future.Future

@Repository
interface WeatherRepository {

    @GET(url = ":weather", maxFreshTime = AlarmManager.INTERVAL_HOUR)
    fun getWeatherData(@UrlParameter weather: String,
                       @UrlQuery("APPID") apiKey: String,
                       @UrlQuery("q") cityCountry: String = "Melbourne,au",
                       @UrlQuery("units") metric: String = "metric"): LiveData<WeatherModel>

    @CUD(url = ":weather", cudType = CudType.POST)
    fun sendTest(): Future<String>

    @CACHE(operation = CacheOperation.INSERT)
    fun insertCache(@CacheBody newCacheKey: String): Future<Boolean>
}