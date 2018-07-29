package org.demo.weatherapp.storage

import org.demo.weatherapp.model.WeatherModel
import repolizer.adapter.StorageAdapter

class AppDatabaseAdapter
constructor(private val weatherModelDao: WeatherModelDao): StorageAdapter<WeatherModel>() {

    override fun insert(repositoryClass: Class<*>, url: String, sql: String, data: Any): Boolean {
        return if(data is String) {
            val model: WeatherModel? = converterAdapter.convertStringToData(repositoryClass, data, WeatherModel::class.java)
            model?.let {
                weatherModelDao.insertWeatherModel(model)
                true
            } ?: false
        } else false
    }

    override fun update(repositoryClass: Class<*>, url: String, sql: String, data: Any) {
        //do nothing
    }

    override fun get(repositoryClass: Class<*>, url: String, sql: String): WeatherModel? {
        return weatherModelDao.getWeather()
    }

    override fun delete(repositoryClass: Class<*>, url: String, sql: String) {
        //do nothing
    }

    override fun canHaveActiveConnections(): Boolean {
        return true
    }

    override fun <Wrapper> establishConnection(repositoryClass: Class<*>, url: String, sql: String): Wrapper? {
        return weatherModelDao.getWeatherAsLiveData() as? Wrapper
    }
}