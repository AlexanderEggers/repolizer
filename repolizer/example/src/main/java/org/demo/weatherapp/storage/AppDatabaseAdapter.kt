package org.demo.weatherapp.storage

import org.demo.weatherapp.model.WeatherModel
import repolizer.adapter.ConverterAdapter
import repolizer.adapter.StorageAdapter
import repolizer.repository.future.FutureRequest

class AppDatabaseAdapter
constructor(private val weatherModelDao: WeatherModelDao): StorageAdapter<WeatherModel>() {

    override fun insert(request: FutureRequest, converter: ConverterAdapter?, data: Any?): Boolean {
        return if(data is String) {
            val model: WeatherModel? = converter?.convertStringToData(request.repositoryClass, data,
                    request.bodyType)
            model?.let {
                weatherModelDao.insertWeatherModel(model)
                true
            } ?: false
        } else false
    }

    override fun update(request: FutureRequest, data: Any?): Boolean {
        //do nothing
        return false
    }

    override fun get(request: FutureRequest, converter: ConverterAdapter?): WeatherModel? {
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