package org.demo.weatherapp.di

import android.content.Context
import archknife.annotation.ProvideModule
import dagger.Module
import dagger.Provides
import org.demo.weatherapp.R
import org.demo.weatherapp.api.WeatherRepository
import repolizer.Repolizer
import javax.inject.Singleton

@ProvideModule
@Module
class NetworkModel {

    @Singleton
    @Provides
    fun provideWeatherRepository(context: Context): WeatherRepository {
        return Repolizer.newBuilder()
                .setBaseUrl(context.getString(R.string.server_base_url))
                .build()
                .getRepository(WeatherRepository::class.java)
    }
}