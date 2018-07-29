package org.demo.weatherapp.storage

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.demo.weatherapp.model.WeatherModel

@Dao
interface WeatherModelDao {

    @Insert
    fun insertWeatherModel(weatherModel: WeatherModel)

    @Query(value = "SELECT * FROM weather_table")
    fun getWeather(): WeatherModel

    @Query(value = "SELECT * FROM weather_table")
    fun getWeatherAsLiveData(): LiveData<WeatherModel>
}