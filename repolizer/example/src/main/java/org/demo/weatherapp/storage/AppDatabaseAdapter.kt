package org.demo.weatherapp.storage

import org.demo.weatherapp.model.WeatherModel
import repolizer.adapter.ConverterAdapter
import repolizer.adapter.StorageAdapter
import java.lang.reflect.Type

class AppDatabaseAdapter
constructor(private val weatherModelDao: WeatherModelDao): StorageAdapter<WeatherModel>() {

    override fun insert(repositoryClass: Class<*>, converter: ConverterAdapter?, url: String,
                         sql: String, data: Any, bodyType: Type): Boolean {
        return if(data is String) {
            val model: WeatherModel? = converter?.convertStringToData(repositoryClass, data, bodyType)
            model?.let {
                weatherModelDao.insertWeatherModel(model)
                true
            } ?: false
        } else false
    }

    override fun update(repositoryClass: Class<*>, sql: String, data: Any?): Boolean {
        //do nothing
        return false
    }

    override fun get(repositoryClass: Class<*>, converter: ConverterAdapter?, url: String,
                     sql: String, bodyType: Type): WeatherModel? {
        return weatherModelDao.getWeather()
    }

    override fun delete(repositoryClass: Class<*>, url: String, sql: String): Boolean {
        //do nothing
        return false
    }

    override fun canHaveActiveConnections(): Boolean {
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun <Wrapper> establishConnection(repositoryClass: Class<*>, url: String, sql: String): Wrapper? {
        return weatherModelDao.getWeatherAsLiveData() as? Wrapper
    }
}