package org.demo.weatherapp.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.demo.weatherapp.model.WeatherModel

@Database(version = 1, entities = [WeatherModel::class])
@TypeConverters(value = [Converter::class])
abstract class AppDatabase: RoomDatabase() {

    abstract fun weatherDao(): WeatherModelDao
}