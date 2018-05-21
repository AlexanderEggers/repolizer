package org.demo.weatherapp.database

import repolizer.annotation.database.Database
import repolizer.annotation.database.TypeConverter
import repolizer.annotation.database.util.DatabaseType

@Database(name = "WeatherDatabase", type = DatabaseType.PERSISTENT, version = 1)
@TypeConverter(value = [Converter::class])
interface AppDatabase
