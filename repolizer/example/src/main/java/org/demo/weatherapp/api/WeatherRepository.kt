package org.demo.weatherapp.api

import android.app.AlarmManager
import android.arch.lifecycle.LiveData
import org.demo.weatherapp.model.WeatherModel
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.UrlParameter
import repolizer.annotation.repository.parameter.UrlQuery

@Repository
interface WeatherRepository {

    @GET(url = ":weather", maxFreshTime = AlarmManager.INTERVAL_HOUR, saveData = false)
    fun getWeatherData(@UrlParameter weather: String,
                       @UrlQuery("APPID") apiKey: String,
                       @UrlQuery("q") cityCountry: String = "Melbourne,au",
                       @UrlQuery("units") metric: String = "metric"): LiveData<WeatherModel>
}