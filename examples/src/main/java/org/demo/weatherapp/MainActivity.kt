package org.demo.weatherapp

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import archknife.annotation.ProvideActivity
import archknife.extension.Injectable
import org.demo.weatherapp.databinding.ActivityMainBinding
import javax.inject.Inject

@ProvideActivity
class MainActivity : AppCompatActivity(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var viewModel: MainActivityViewModel? = null

    var binding: ActivityMainBinding? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(MainActivityViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding!!.viewModel = viewModel
    }
}
