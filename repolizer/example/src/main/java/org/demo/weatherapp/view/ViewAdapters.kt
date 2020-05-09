package org.demo.weatherapp.view

import android.text.Html
import android.widget.TextView
import androidx.databinding.BindingAdapter

@Suppress("DEPRECATION")
@BindingAdapter("setWeatherIconView")
fun setWeatherIconView(textView: TextView, value: String?) {
    if (value != null && value.isNotEmpty()) {
        textView.text = Html.fromHtml(value)
    }
}