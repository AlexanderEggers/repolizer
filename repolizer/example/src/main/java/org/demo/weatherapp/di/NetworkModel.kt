package org.demo.weatherapp.di

import android.content.Context
import archknife.annotation.ProvideModule
import archtree.helper.AppExecutor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.demo.weatherapp.R
import org.demo.weatherapp.api.WeatherRepository
import org.demo.weatherapp.storage.AppDatabaseAdapterFactory
import repolizer.Repolizer
import repolizer.adapter.cache.sharedprefs.SharedPrefCacheAdapterFactory
import repolizer.adapter.converter.gson.GsonConverterAdapterFactory
import repolizer.adapter.wrapper.livedata.LiveDataWrapperFactory
import repolizer.adapter.network.retrofit.RetrofitNetworkAdapterFactory
import javax.inject.Singleton

@ProvideModule
@Module
class NetworkModel {

    @Singleton
    @Provides
    fun provideWeatherRepository(context: Context, appExecutor: AppExecutor, httpClient: OkHttpClient): WeatherRepository {
        return Repolizer.newBuilder()
                .setBaseUrl(context.getString(R.string.server_base_url))
                .addNetworkAdapter(RetrofitNetworkAdapterFactory(context.getString(R.string.server_base_url),
                        httpClient = httpClient))
                .addWrapperAdapter(LiveDataWrapperFactory())
                .addStorageAdapter(AppDatabaseAdapterFactory(context))
                .addCacheAdapter(SharedPrefCacheAdapterFactory(context))
                .addConverterAdapter(GsonConverterAdapterFactory())
                .setDefaultMainThread(appExecutor.mainThread)
                .build()
                .getRepository(WeatherRepository::class.java)
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