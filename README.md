Repolizer
=====
[![Download](https://api.bintray.com/packages/mordag/android/repolizer-core/images/download.svg) ](https://bintray.com/mordag/android/repolizer-core/_latestVersion)

Repolizer is generating the backend related classes for your Android project. Backends are usually complex, containing a lot of potential errors and consuming most of the development time. The generated backend includes databases, webservices, caching and repositories.

Since the release of Android Room most of the required work for backends has been simplified. Also the usage of Retrofit and Dagger helped people to decrease the development time in creating app backends. But there's still a lot of effort and time that needs to be put into creating a fully working backend.

Repolizer provides you with a solution for that using a handful of annotations. There are two different areas this library will try to tackle. The library is using Retrofit and Room internally to simplify several implementations and to keep the generated classes to a minimum.

If you need more information in how to use the library, take a look inside the [wiki][4].

Download
--------
```gradle
repositories {
  jcenter()
  google()
}

dependencies {
  def repolizer_version = "0.3.0"

  //includes all library artifacts and required classes (this artifact is required if you want to use the processor)
  implementation "org.repolizer:repolizer-core:$repolizer_version"
  //just annotations
  implementation "org.repolizer:repolizer-annotation:$repolizer_version"
  
  kapt "org.repolizer:repolizer-processor:$repolizer_version"
  
  //the library requires you to use add the Room compiler dependency to your project (that will change with the next release)
  def room_version = "1.1.1-rc1"
  kapt "android.arch.persistence.room:compiler:$room_version"
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
Version 1.0.0 is currently under development in the master branch.

Comments/bugs/questions/pull requests are always welcome!

Compatibility
-------------

 * The library requires at minimum Android 14.

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
