package org.demo.weatherapp.database

import repolizer.annotation.database.Database
import repolizer.annotation.database.Migration
import repolizer.annotation.database.TypeConverter
import repolizer.annotation.database.util.DatabaseType
import repolizer.annotation.database.util.MigrationType

@Database(name = "WeatherDatabase", type = DatabaseType.PERSISTENT, version = 2)
@TypeConverter(typeConverter = [Converter::class])
@Migration(migrationType = MigrationType.DESTRUCTIVE)
interface AppDatabase