package org.demo.weatherapp.view

import android.databinding.BindingAdapter
import android.text.Html
import android.widget.TextView

class ViewAdapters {

    companion object {

        @JvmStatic
        @BindingAdapter("setWeatherIcon")
        fun setWeatherIcon(textView: TextView, value: String?) {
            if (value != null && !value.isEmpty()) {
                textView.text = Html.fromHtml(value)
            }
        }
    }
}
