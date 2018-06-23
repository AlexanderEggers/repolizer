Change Log
==========

Version 0.3.0 *(2018-06-12)*
----------------------------

#### ANNOTATION

- **NEW:** Made the field 'name' for the Database annotation optionally. The default value is "database". (526ba2e4583075b1b5047c953be45be491f63312)
- **NEW:** Renamed field 'value' to 'typeConverter' for the TypeConverter annotation. (45d76fa433f10d4c7cabf9926fa16e446bb3a117)
- **NEW:** Removed the field entityBodyAsList from the CUD annotation. The internal implementation of the CUD network builder will require a serializable object to simplify it's usage. Therefore if you use the CUD annotation, your annotated RequestBody object needs to implement serializable. (f1847f3d17c57cc5233c3913b34c08576c84d582)
- **NEW:** Added a new field to the DB annotation which allows the user to define the onConflict strategy. This stategy will only be used for the database operation INSERT and UPDATE. (cbc8994b0aef5309b3b841d9968171e0da2f4424)
- **NEW:** Made the field 'url' for the GET annotation optionally. If empty the GET method will only return the current saved cache in the database. (5c338e28482e8ac4676b318b509fd7ac16f4539c)
- **NEW:** Made the field 'sql' for the DB annotation optionally. (5c338e28482e8ac4676b318b509fd7ac16f4539c)
- **NEW:** Renamed field 'sql' to 'querySql' for the GET annotation. If the field value is empty, the GET method will use the default sql query "SELECT * FROM tablename" (5c338e28482e8ac4676b318b509fd7ac16f4539c)
- **NEW:** Added new repository parameter which should handle the case if the cache is too old but the GET method was unable to retrieve new content (default will delete cache). The relevant value has been added to the Repository annotation. The value can also be set for each method by using the RepositoryParameter annotation (using the value 'DELETE_IF_CACHE_TOO_OLD'). (5c338e28482e8ac4676b318b509fd7ac16f4539c)
- **NEW:** Added new field 'deleteSql' to the GET annotation. This field will define the optional sql query if the repository needs to delete something (like if the cache is too old). This event can be defined by using the new repository parameter (DELETE_IF_CACHE_TOO_OLD) and/or using the new field annotated inside the Repository annotation (5c338e28482e8ac4676b318b509fd7ac16f4539c).
- **NEW:** Added onConflict stragtegy to the GET and REFRESH annotation. (21a947fece51fb0c16e96017b3fb9e7f20357e15)
- **NEW:** Added new Progress annotation which can be used for method parameter to define certain values for your ProgressController. This could include a title, message, color or what ever you feel like. The value for the Progress annotation needs to be an object which extends ProgressData. To this object you can assign the required progress values. (5bf5ae77be6796fb0484e077d1467c2c89b7f64f)

#### CORE
- **NEW**: Refactored most of the classes in the core artifact to avoid kotlin-NPE and clean up the implementations.
- **NEW:** Moved several classes to new/different packages.
- **NEW:** Refactored ProgressController. The class has now an internal map to ensure that the progress is only closed if no requests are running. Cancel is still working as expected. The new implementation is also responsible to handle the new Progress annotation. Therefore the abstract method onShow is getting the ProgressData object (when if none is set that will only include the RequestType). The ProgressController got a new method called resetController that should be used as soon as the user switches to a new actviity (or even fragment). Usually you don't want to block the user interaction if the progress is displayed. Therefore if the user is switching the context, the progress is not visible anymore and would block any if the progress that the new activity/fragment would show. (d60bf3457167de34168dc7c5bf3167a2762da1f3, 959a0b5092198fb8505117db584c62dfaee6436f)
- **BUGFIX:** Changed the workerThread of the AppExecutor from newSingleThreadExecutor to newCachedThreadPool. This should increase the performance of your app significantly. (fbc29648883f556b238499fbf1fdb7ee154a8d90)
- **BUGFIX:** Fixed several JVM related issues that would generate wired getter/setter for kotlin files.
- **BUGFIX:** Added missing JvmStatic annotation to the newBuilder method inside the Repolizer class.

#### PROCESSOR
- **NEW:** Refactored every processor-related class to avoid possible NPE and to create a cleaner code structure.
- **NEW:** Added several error messages to the processor to inform the user better about possible annotation related errors.
- **NEW:** Changed the required return value for the Repository implementation. 
  * **GET annotation:** requires either LiveData\<ENTITY\> or LiveData\<List\<ENTITY\>\>. This can be adjusted using the GET annotation and defining the getAsList field (default = true). ENTITY stands for the class which you have defined inside the Repository annotation using the entity field.
  * **REFRESH annotation:** requires a LiveData\<Boolean\>
  * **CUD annotation:** requires a LiveData\<String\>
  * **DB annotation:** requires a LiveData\<Boolean\> or void
  * **CACHE annotation:** requires a LiveData\<Boolean\> or void
- **BUGFIX:** Fixed missing adding query to url for the refresh/get method processor part.

#### MISC
- Updated gson to version 2.8.5.

Version 0.2.1 *(2018-06-01)*
----------------------------

- **NEW**: Added some compiler messages for the usage of the DB annotation >> in case the DB annotation has been used without any parameter (f68cd0795781baec78a78228d988fc03068f419b)
- **BUGFIX**: Added missing DatabaseBody implementation for the DB annotation (for INSERT, UPDATE, DELETE) (f68cd0795781baec78a78228d988fc03068f419b)
- **BUGFIX**: Added some conditions to make sure that the DB annotation is only using the relevant parameter. INSERT, UPDATE, DELETE are using the DatabaseBody annotation. QUERY is using the SqlParameter annotation. (f68cd0795781baec78a78228d988fc03068f419b)
- **MISC**: Updated Room to version 1.1.1-rc1. This version fixes some major crashes when using database migration. (f68cd0795781baec78a78228d988fc03068f419b)

Version 0.2.0 *(2018-06-01)*
----------------------------

- **NEW**: Changed the dependency of certain libraries which are used internally so that any other project does not need to implement those too. (2c22b55fde177e94059a0288a237b4f6c186dc3b)
- **NEW**: Added missing implementation so that the url query strings are included to the actual url for the caching. (dd9c532b4fe1dd03f4a87048bd14586acee27e9f)
- **NEW**: Moved prepareUrl from NetworkController to NetworkResource to simplify the usage of implementing custom NetworkController. (bd6cc09086f88ea6e2f58800872890d49fb431bd)
- **NEW**: Added support for different database operations when using the DB annotation. The annotation requires you to define a DatabaseOperation enum value. This value is used to create a certain method inside the dao which is used by this specific method. Database operations can be insert, update, query and delete. (34b514ff9d3bfc9879334342a14ab0eda09771c6)
- **BUGFIX**: Added some missing processor information. (6cf91cbe85f99aaad6404eb1ec4fd52128e01019)
- **BUGFIX**: Fixed prepareUrl that is used to remove all queries from the url. (5304a78066c430ff2ae200b6f304b1a168009e1b) 
- **MISC**: Decreased minimum SDK level from 16 to 14. (2c22b55fde177e94059a0288a237b4f6c186dc3b)
- **MISC**: Updated javapoet to version 1.11.1. (34b514ff9d3bfc9879334342a14ab0eda09771c6)

Version 0.1.0 *(2018-05-22)*
----------------------------

- Initial library release.