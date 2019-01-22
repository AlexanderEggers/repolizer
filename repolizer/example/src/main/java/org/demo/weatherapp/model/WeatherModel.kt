package org.demo.weatherapp.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "weather_table")
class WeatherModel {

    @PrimaryKey
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("name")
    var cityName: String? = null

    @SerializedName("weather")
    var condition: ArrayList<ConditionModel>? = null

    @ColumnInfo(name = "data_time")
    @SerializedName("dt")
    var dataTime: Long = 0

    @Embedded
    @SerializedName("main")
    var data: DataModel? = null

    @Embedded
    @SerializedName("sys")
    var systemData: SystemModel? = null
}