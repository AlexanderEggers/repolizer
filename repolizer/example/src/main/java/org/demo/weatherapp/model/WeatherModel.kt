package org.demo.weatherapp.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "weather_data")
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