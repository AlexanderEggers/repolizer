package org.demo.weatherapp.api

import android.app.AlarmManager
import android.arch.lifecycle.LiveData
import org.demo.weatherapp.database.AppDatabase
import org.demo.weatherapp.model.WeatherModel
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.REFRESH
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.UrlParameter
import repolizer.annotation.repository.parameter.UrlQuery

@Repository(entity = WeatherModel::class, database = AppDatabase::class, tableName = "weather_data")
interface WeatherRepository {

    @REFRESH(url = ":weather", getAsList = false)
    fun refreshWeatherData(@UrlParameter weather: String,
                           @UrlQuery("APPID") apiKey: String,
                           @UrlQuery("q") cityCountry: String = "Melbourne,au",
                           @UrlQuery("units") metric: String = "metric"): LiveData<Boolean>

    @GET(url = ":weather", getAsList = false, maxFreshTime = AlarmManager.INTERVAL_HOUR)
    fun getWeatherData(@UrlParameter weather: String,
                       @UrlQuery("APPID") apiKey: String,
                       @UrlQuery("q") cityCountry: String = "Melbourne,au",
                       @UrlQuery("units") metric: String = "metric"): LiveData<WeatherModel>
}