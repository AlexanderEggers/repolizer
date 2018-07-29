package org.demo.weatherapp.storage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.demo.weatherapp.model.WeatherModel

@Database(version = 1, entities = [WeatherModel::class])
@TypeConverters(value = [Converter::class])
abstract class AppDatabase: RoomDatabase() {

    abstract fun weatherDao(): WeatherModelDao
}