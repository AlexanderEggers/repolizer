package org.demo.weatherapp.model

import com.google.gson.annotations.SerializedName

class DataModel {

    @SerializedName("temp")
    var temp: Double = 0.0

    @SerializedName("humidity")
    var humidity: String? = null

    @SerializedName("pressure")
    var pressure: String? = null
}