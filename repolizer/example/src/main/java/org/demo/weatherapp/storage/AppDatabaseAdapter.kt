package org.demo.weatherapp.storage

import org.demo.weatherapp.model.WeatherModel
import repolizer.adapter.DataAdapter
import repolizer.repository.future.FutureRequest

class AppDatabaseAdapter
constructor(private val weatherModelDao: WeatherModelDao): DataAdapter<WeatherModel>() {

    override fun insert(request: FutureRequest, data: WeatherModel?): Boolean {
        return data?.let {
            weatherModelDao.insertWeatherModel(data)
            true
        } ?: false
    }

    override fun update(request: FutureRequest, data: WeatherModel?): Boolean {
        //do nothing
        return false
    }

    override fun get(request: FutureRequest): WeatherModel? {
        return weatherModelDao.getWeather()
    }

    override fun delete(request: FutureRequest): Boolean {
        //do nothing
        return false
    }

    override fun canHaveActiveConnections(): Boolean {
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun <Wrapper> establishConnection(request: FutureRequest): Wrapper? {
        return weatherModelDao.getWeatherAsLiveData() as? Wrapper
    }
}