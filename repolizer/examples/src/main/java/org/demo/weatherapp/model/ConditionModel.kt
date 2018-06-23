package org.demo.weatherapp.model

import com.google.gson.annotations.SerializedName

class ConditionModel {

    @SerializedName("id")
    var conditionId: Int = 0

    @SerializedName("description")
    var description: String? = null
}