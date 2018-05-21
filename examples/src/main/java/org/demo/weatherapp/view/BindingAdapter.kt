package org.demo.weatherapp.view

import android.content.Context
import android.graphics.Typeface

class BindingAdapter {

    companion object {

        @JvmStatic
        fun setWeatherFont(context: Context): Typeface {
            return Typeface.createFromAsset(context.assets, "fonts/weathericons-regular-webfont.ttf")
        }
    }
}
