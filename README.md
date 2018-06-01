Repolizer
=====
[![Download](https://api.bintray.com/packages/mordag/android/repolizer-core/images/download.svg) ](https://bintray.com/mordag/android/repolizer-core/_latestVersion)

Repolizer has the goal to generate backend related classes for your Android project. Backends are usually complex, containing a lot of potential errors and consuming most of the development time.

Since the release of Android Room, most of this required work has been simplified. Also the usage of Retrofit and Dagger helped people to decrease the development time in creating app backends. But you still need to put a lot of effort and time into creating a fully working backend (and don't get me started with testing this). On top of that, if you even want to create something like a MVVM based app, you will have to structure and develop even more for your backend. 

Repolizer provides you with a solution for that using a handful of annotations. There are two different areas this library will try to tackle. First the repositories that are required for your MVVM-pattern and a database. The library is using Retrofit and Room to simplify several implementations and to keep the generated classes to a minimum.

Download
--------
```gradle
repositories {
  jcenter()
  google()
}

dependencies {
  //includes all library artifacts
  implementation 'org.repolizer:repolizer-core:0.2.1'
  //just annotations
  implementation 'org.repolizer:repolizer-annotation:0.2.1'
  
  kapt 'org.repolizer:repolizer-processor:0.2.1'
  
  //Because of Room, you need one additional processor dependency inside your project:
  kapt "android.arch.persistence.room:compiler:1.1.1-rc1"
}
```

How do I use Repolizer? (Step-by-step introduction for 0.2.1)
-------------------
Repolizer's design has been created similar to the Retrofit and Room usage. Therefore you need to create interfaces that will represent your repositories and databases.

The repository includes the annotations GET, CUD, REFRESH, CACHE and DB. GET and CUD (create=PUT, update=PUSH, delete=DELETE) are related to their http codes. Every GET method will create a connection between the holder (like your viewmodel) and the database entries. The webservice will be used to update the database which will update this connection. Therefore REFRESH can be used to update the database entries after establishing the connection using the related GET method. The DB annotation can be used to create repository methods to change certain things inside the database. CACHE can be used to execute a small range of operations on the current saved cache. DB and CACHE require you to set an enum which defines the operation you can execute using the annotation. Optionally the DB annotation allows you to set a custom sql string. Keep in mind that certain operations require you to set a parameter using the DatabaseBody or SqlParameter annotation. For example the INSERT operation requires you to define an object which annotates the DatabaseBody which will be writtin inside the database (more you will find inside the documentation - coming soon!).

The repository requires an entitiy, a database and a table name. Therefore every repository should be reponsible for only ONE entitiy. The database is used to define which source will save any data from this repository and the table is used for the related DAO object. 

**Important:** Any class that will be connected as an entitiy to a repository needs to include addtional annotations which are required by Android Room. This includes the @Entitiy(tableName:String) annotation and the definition of the @PrimaryKey and @ColumnInfo. These annotations are used to define the database structure for this specific entity. **The table name which you define for the entity needs to be the same as it's related repository table name (else Room will throw errors)!**

Each repository has a cache system that uses two different values: maxCacheTime and freshTime. maxCacheTime is the time that a specific cache will be stored inside the database. freshtime is used to define a time from when the webservice will start trying to retrive new data from the server. If that is not possible it will use the cache as long as the maxCacheTime is not reached. These values can be defined for every GET method.

Each repository method has a specific rule regarding the return value. Each method, except the GET, DB and CACHE methods, needs to return LiveData<String>. Usually the return value for the non-GET methods is not really excting anyway. But in case you need the response of the webservice to your non-GET url, the LiveData object will include the response of your request. The GET method can have two types of return values: LiveData<List<T>> or LiveData<T>. The 'T' stands for your entitiy that the repository represents. Therefore the return value can include a list or a single object. Addition to defining this return value correctly, you need to set a flag (getAsList) inside the GET method annotation that will tell the underlying system your choice for the return value (true by default). The methods annotating DB and CACHE cannot return anything! Those methods are only used to execute certain operations on it's databases.

Here's an example for a repository:

```kotlin
@Repository(entity = DemoModel::class, database = DemoDatabase::class, tableName = "demo_table")
interface DemoRepository {

    @GET(url = "myUrl/moreUrlStuff")
    fun getData(@UrlQuery("apiKey") apiKey: String,
                @UrlQuery("q") myQuery: String): LiveData<List<DemoModel>>
}
```
The database requires certain information inside it's annotation. That includes it's file name, migration strategies, JournalMode and more. You can use @Database, @Migration and @TypeConverter for that.

Here's an example for a database:

```kotlin
@Database(name = "DemoDatabase", type = DatabaseType.PERSISTENT, version = 1)
@TypeConverter(value = [Converter::class])
@Migration(migrationType = MigrationType.DEFAULT)
interface DemoDatabase
```
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
