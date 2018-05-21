package org.demo.weatherapp.model

import com.google.gson.annotations.SerializedName

class SystemModel {

    @SerializedName("country")
    var country: String? = null

    @SerializedName("sunrise")
    var sunrise: Long = 0

    @SerializedName("sunset")
    var sunset: Long = 0
}