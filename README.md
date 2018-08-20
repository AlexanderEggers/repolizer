Repolizer
=====
[![Download](https://api.bintray.com/packages/mordag/android/repolizer-core/images/download.svg) ](https://bintray.com/mordag/android/repolizer-core/_latestVersion)

Repolizer is generating the backend related classes for your Android/Java project. Backends are usually complex, containing a lot of potential errors and consuming most of the development time. The generated backend includes databases, webservices, caching and repositories.

Since the release of Android Room most of the required work for backends has been simplified. Also the usage of Retrofit and Dagger helped people to decrease the development time in creating app backends. But there's still a lot of effort and time that needs to be put into creating a fully working backend.

Repolizer provides you with a solution for that using a handful of annotations and adapters. The library is designed to give you access to a full stack of implementations (caching, network, persisting).

If you need more information in how to use the library, take a look inside **[this wiki][4]**. Due to the latest changes (0.4.0) the wiki is not up-to-date, you should therefore use the [example project][3] to get an idea how to use the library.

Download
--------
```gradle
repositories {
  jcenter()
  google()
}

dependencies {
  def repolizer_version = "0.4.0"

  //includes all library artifacts and required classes (this artifact is required if you want to use the processor)
  implementation "org.repolizer:repolizer-core:$repolizer_version"
  //just annotations
  implementation "org.repolizer:repolizer-annotation:$repolizer_version"
  //livedata wrapper adapter dependency
  implementation "org.repolizer:wrapper-livedata:$repolizer_version"
  //retrofit network adapter dependency
  implementation "org.repolizer:network-retrofit:$repolizer_version"
  //sharedprefs cache adapter dependency
  implementation "org.repolizer:cache-sharedprefs:$repolizer_version"
  //gson converter adapter dependency
  implementation "org.repolizer:converter-gson:$repolizer_version"
  
  kapt "org.repolizer:repolizer-processor:$repolizer_version"
}
```

Example project
-------------------

If you need a fully working example, please use the [example project][3]. In order to make it work you need to register your own api-key for the application at https://openweathermap.org/api and define it's key inside the local.properties file of the project.

```
apiKey= MY_API_KEY
```

Status
------
Version 1.0.0 is currently under development in the master branch. The latest version 0.4.0 can be found in the master branch.

Comments/bugs/questions/pull requests are always welcome!

Compatibility
-------------

 * The library requires at minimum Java 7.

Author
------
Alexander Eggers - [@mordag][2] on GitHub

License
-------
Apache 2.0. See the [LICENSE][1] file for details.


[1]: https://github.com/Mordag/repolizer/blob/master/LICENSE
[2]: https://github.com/Mordag
[3]: https://github.com/Mordag/repolizer/tree/master/examples/src/main/java/org/demo/weatherapp
[4]: https://github.com/Mordag/repolizer/wiki
