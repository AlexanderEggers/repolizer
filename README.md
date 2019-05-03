Repolizer
=====
[![Download](https://api.bintray.com/packages/mordag/android/repolizer-core/images/download.svg) ](https://bintray.com/mordag/android/repolizer-core/_latestVersion)

Repolizer is generating the backend related classes for your Android/Java project. Backends are usually complex, containing a lot of potential errors and consuming most of the development time.

Repolizer provides you with a solution using a handful of annotations and adapters. The library is designed to give you access to a full stack of implementations (downloading, caching, converting, persisting). You can use the default implementations or write your own adapters (like if you want to use your own http client).

Download
--------
```gradle
repositories {
  jcenter()
}

dependencies {
  def repolizer_version = "0.8.1"

  //main dependency which is required for the app
  implementation "org.repolizer:repolizer-core:$repolizer_version"
  
  //just annotations
  implementation "org.repolizer:repolizer-annotation:$repolizer_version"
  //optional livedata wrapper adapter dependency
  implementation "org.repolizer:wrapper-livedata:$repolizer_version"
  //optional retrofit network adapter dependency
  implementation "org.repolizer:network-retrofit:$repolizer_version"
  //optional sharedprefs cache adapter dependency
  implementation "org.repolizer:cache-sharedprefs:$repolizer_version"
  //optional gson converter adapter dependency
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

ProGuard
------
```
-keep public class * extends repolizer.adapter.network.retrofit.api.NetworkController { *; }
-keep public class * extends repolizer.repository.BaseRepository { *; }
-keep @repolizer.annotation.repository.Repository public class * { *; }
```

Status
------
Version 1.0.0 is currently under development in the master branch.

Comments/bugs/questions/pull requests are always welcome!

Compatibility
-------------

 * The library requires at minimum Java 7.
 * Some Repolizer adapter require a minimum Android SDK level of 16 and the usage of AndroidX instead of the legacy Android support libary.

Author
------
Alexander Eggers - [@mordag][2] on GitHub

License
-------
Apache 2.0. See the [LICENSE][1] file for details.


[1]: https://github.com/Mordag/repolizer/blob/master/LICENSE
[2]: https://github.com/Mordag
[3]: https://github.com/Mordag/repolizer/tree/master/repolizer/example
[4]: https://github.com/Mordag/repolizer/wiki
