package org.demo.weatherapp.storage

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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