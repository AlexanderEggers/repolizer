package org.demo.weatherapp.api

import android.app.AlarmManager
import android.arch.lifecycle.LiveData
import org.demo.weatherapp.model.WeatherModel
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.Progress
import repolizer.annotation.repository.parameter.UrlParameter
import repolizer.annotation.repository.parameter.UrlQuery
import repolizer.repository.progress.ProgressData

@Repository
interface WeatherRepository {

    @GET(url = ":weather", maxFreshTime = AlarmManager.INTERVAL_HOUR)
    fun getWeatherData(@Progress test: ProgressData = ProgressData(), @Progress test2: ProgressData = ProgressData(), @UrlParameter weather: String,
                       @UrlQuery("APPID") apiKey: String,
                       @UrlQuery("q") cityCountry: String = "Melbourne,au",
                       @UrlQuery("units") metric: String = "metric"): LiveData<WeatherModel>
}