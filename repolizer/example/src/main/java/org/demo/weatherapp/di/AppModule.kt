package org.demo.weatherapp.di

import android.app.Application
import android.content.Context
import archknife.annotation.ProvideModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@ProvideModule
@Module
class AppModule {

    @Singleton
    @Provides
    fun provideAppContext(app: Application): Context {
        return app.applicationContext
    }
}