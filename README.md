Repolizer
=====
[![Download](https://api.bintray.com/packages/mordag/android/repolizer-core/images/download.svg) ](https://bintray.com/mordag/android/repolizer-core/_latestVersion)

Repolizer is an Android annotation processor which can be used to add a MVVM-based repository pattern to your App.

Download
--------
```gradle
repositories {
  jcenter()
}

dependencies {
  //includes all library artifacts
  implementation 'org.repolizer:repolizer-processor:0.1.0'
  //just annotations
  implementation 'org.repolizer:repolizer-annotation:0.1.0'
  
  kapt 'org.repolizer:repolizer-processor:0.1.0'
}
```

How do I use Repolizer? (Step-by-step introduction for 0.1.0)
-------------------
Coming soon! For now take a look at the [example project][3].

Status
------
Version 1.0.0 is currently under development in the master branch.

Comments/bugs/questions/pull requests are always welcome!

Compatibility
-------------

 * The library requires at minimum Android 16.

Author
------
Alexander Eggers - [@mordag][2] on GitHub

License
-------
Apache 2.0. See the [LICENSE][1] file for details.


[1]: https://github.com/Mordag/repolizer/blob/master/LICENSE
[2]: https://github.com/Mordag
[3]: https://github.com/Mordag/repolizer/tree/master/examples/src/main/java/org/demo/weatherapp
