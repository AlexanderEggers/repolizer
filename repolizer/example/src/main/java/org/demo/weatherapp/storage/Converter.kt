package org.demo.weatherapp.storage

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.demo.weatherapp.model.ConditionModel
import java.util.*

class Converter {

    @TypeConverter
    fun fromString(value: String): ArrayList<ConditionModel> {
        val listType = object : TypeToken<ArrayList<ConditionModel>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<ConditionModel>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}