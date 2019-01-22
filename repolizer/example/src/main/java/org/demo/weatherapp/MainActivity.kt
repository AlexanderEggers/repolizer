package org.demo.weatherapp

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import archknife.annotation.ProvideActivity
import archknife.annotation.util.Injectable
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
