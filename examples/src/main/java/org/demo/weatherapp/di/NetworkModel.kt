package org.demo.weatherapp.di

import android.content.Context
import archknife.annotation.ProvideModule
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.demo.weatherapp.R
import org.demo.weatherapp.api.WeatherRepository
import repolizer.Repolizer
import javax.inject.Singleton

@ProvideModule
@Module
class NetworkModel {

    @Singleton
    @Provides
    fun provideWeatherRepository(context: Context, okHttpClient: OkHttpClient): WeatherRepository {
        return Repolizer.newBuilder()
                .setBaseUrl(context.getString(R.string.server_base_url))
                .setClient(okHttpClient)
                .build(context)
                .create(WeatherRepository::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
    }
}