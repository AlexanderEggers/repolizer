package org.demo.weatherapp

import archknife.annotation.ProvideActivity
import archtree.activity.ActivityBuilder
import archtree.activity.ActivityResource
import archtree.activity.ArchTreeActivity

@ProvideActivity
class MainActivity : ArchTreeActivity<MainActivityViewModel>() {

    override fun provideActivityResource(builder: ActivityBuilder<MainActivityViewModel>): ActivityResource<MainActivityViewModel> {
        return builder.setViewModel(MainActivityViewModel::class.java, BR.viewModel)
                .setHideSupportBar(true)
                .setLayoutId(R.layout.activity_main)
                .build()
    }
}
