package org.demo.weatherapp.storage

import android.content.Context
import repolizer.Repolizer
import repolizer.adapter.factory.AdapterFactory
import java.lang.reflect.Type
import android.arch.persistence.room.Room

class AppDatabaseAdapterFactory(context: Context): AdapterFactory<AppDatabaseAdapter> {

    private val weatherModelDao: WeatherModelDao

    init {
        val db = Room.databaseBuilder(context,
                AppDatabase::class.java, "app_example_db").build()
        weatherModelDao = db.weatherDao()
    }

    override fun get(type: Type, repositoryClass: Class<*>, repolizer: Repolizer): AppDatabaseAdapter? {
        return AppDatabaseAdapter(weatherModelDao)
    }
}