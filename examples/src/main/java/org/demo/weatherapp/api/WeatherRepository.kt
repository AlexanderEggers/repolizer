package org.demo.weatherapp.api

import android.arch.lifecycle.LiveData
import org.demo.weatherapp.database.AppDatabase
import org.demo.weatherapp.model.WeatherModel
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.UrlQuery

@Repository(entity = WeatherModel::class, database = AppDatabase::class, tableName = "weather_data")
interface WeatherRepository {

    @GET(url = "weather", getAsList = false)
    fun getWeatherData(@UrlQuery("APPID") apiKey: String,
                       @UrlQuery("q") cityCountry: String = "Melbourne,au",
                       @UrlQuery("units") metric: String = "metric"): LiveData<WeatherModel>
}