package org.demo.weatherapp.di

import android.content.Context
import archknife.annotation.ProvideModule
import dagger.Module
import dagger.Provides
import org.demo.weatherapp.R
import org.demo.weatherapp.api.WeatherRepository
import repolizer.Repolizer
import repolizer.adapter.livedata.LiveDataWrapperFactory
import repolizer.adapter.retrofit.RetrofitNetworkAdapterFactory
import javax.inject.Singleton

@ProvideModule
@Module
class NetworkModel {

    @Singleton
    @Provides
    fun provideWeatherRepository(context: Context): WeatherRepository {
        return Repolizer.newBuilder()
                .setBaseUrl(context.getString(R.string.server_base_url))
                .addNetworkAdapter(RetrofitNetworkAdapterFactory(context.getString(R.string.server_base_url)))
                .addWrapperAdapter(LiveDataWrapperFactory())
                .build()
                .getRepository(WeatherRepository::class.java)
    }
}