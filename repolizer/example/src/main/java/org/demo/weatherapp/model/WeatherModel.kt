package org.demo.weatherapp.model

import com.google.gson.annotations.SerializedName

class WeatherModel {

    @SerializedName("id")
    var id: Int = 0

    @SerializedName("name")
    var cityName: String? = null

    @SerializedName("weather")
    var condition: ArrayList<ConditionModel>? = null

    @SerializedName("dt")
    var dataTime: Long = 0

    @SerializedName("main")
    var data: DataModel? = null

    @SerializedName("sys")
    var systemData: SystemModel? = null
}