package org.demo.weatherapp

import android.os.Bundle
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import archknife.annotation.ProvideViewModel
import archtree.viewmodel.BaseViewModel
import org.demo.weatherapp.api.WeatherRepository
import org.demo.weatherapp.model.WeatherModel
import org.demo.weatherapp.util.WeatherIconUtil
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@ProvideViewModel
class MainActivityViewModel
@Inject constructor(private val weatherRepository: WeatherRepository) : BaseViewModel() {

    private val weatherRawData: LiveData<WeatherModel> = weatherRepository.getWeatherData("weather", BuildConfig.apiKey)
    private val observer: Observer<WeatherModel> = Observer {
        if (it != null) {
            cityName.set(it.cityName!!.toUpperCase(Locale.US) + ", " + it.systemData!!.country)
            temp.set(String.format("%.2f", it.data!!.temp) + "°")
            description.set(it.condition!![0].description!!.toUpperCase(Locale.US))
            humidity.set("Humidity: " + it.data!!.humidity + "%")
            pressure.set("Pressure: " + it.data!!.pressure + " hPa")

            val df = DateFormat.getDateTimeInstance()
            updateTime.set("Last update: " + df.format(Date(it.dataTime * 1000)))

            weatherIcon.set(WeatherIconUtil.setWeatherIcon(
                    it.condition!![0].conditionId,
                    it.systemData?.sunrise!! * 1000,
                    it.systemData?.sunset!! * 1000)
            )
        }
    }

    val cityName: ObservableField<String> = ObservableField()
    val updateTime: ObservableField<String> = ObservableField()
    val weatherIcon: ObservableField<String> = ObservableField()
    val temp: ObservableField<String> = ObservableField()
    val description: ObservableField<String> = ObservableField()
    val humidity: ObservableField<String> = ObservableField()
    val pressure: ObservableField<String> = ObservableField()

    override fun onInit(resourceBundle: Bundle?, customBundle: Bundle?, savedInstanceBundle: Bundle?) {
        super.onInit(resourceBundle, customBundle, savedInstanceBundle)
        weatherRawData.observeForever(observer)
        weatherRepository.insertCache("test").execute()
    }

    override fun onCleared() {
        super.onCleared()
        weatherRawData.removeObserver(observer)
    }
}