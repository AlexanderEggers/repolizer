package org.demo.weatherapp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import archknife.annotation.ProvideViewModel
import org.demo.weatherapp.api.WeatherRepository
import org.demo.weatherapp.model.WeatherModel
import org.demo.weatherapp.util.WeatherIconUtil
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@ProvideViewModel
class MainActivityViewModel @Inject constructor(weatherRepository: WeatherRepository) : ViewModel() {

    private val weatherRawData: LiveData<WeatherModel> = weatherRepository.getWeatherData(BuildConfig.apiKey)
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

    init {
        weatherRawData.observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()
        weatherRawData.removeObserver(observer)
    }
}