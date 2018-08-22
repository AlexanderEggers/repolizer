Changelog
==========

Version 0.4.1 *(2018-08-22)*
----------------------------
- **NEW:** The future class got a default method to run async. This method will use the internal RepositoryThread (single thread). The goal is to leave the app worker thread clear from any repository based work to prevent blocking any work for those. Repository thread normally could run for a longer period, especially if webserver call is slow or the response a quite large file to convert. **Note: You can still assign your own threading to the library by using the existing Future.execute() method which runs synchronized.**
- **NEW:** AppExecutor for livedata wrapper adapter will use the new Future.executeAsync() instead of a local excutor singleton.
- **BUGFIX:** Ensured that CacheAdapter is optional.
- **BUGFIX:** Fixed bug that was still using network if the get builder url is empty.
- **BUGFIX:** Ensured network adapter is only needed if builder url is given.
- **MISC:** Updated kotlin to version 1.2.61.

Version 0.4.0 *(2018-08-09)*
----------------------------
With this release I have refactored the whole project to have a more flexible implementation.

### Breaking changes:
- Repolizer-core is now a java library and is therefore producing a jar instead of aar file.
- The Room default implementation has been removed from the project (for now). Because of the major changes, I have to rewrite the whole processor in how database files are being created. Therefore all database related annotations has been removed.
- The annotations for Repository has been changed regarding which fields are included. That change will break your code.
- The DB annotation has been renamed to Storage
- The Repolizer class has dropped several builder methods. You won't be able to set a gson object or context via this builder.
- Internally Repolizer's service classes (like ResponseService) which allows a custom implementation, has been changed. The original implementation is still there but the changes includes methods renaming or simply fixing bugs.

### Summary of the changes:
- The library is now supporting adapters. Those adapters are used for different areas within the library, like network. You can use adapters to define your own custom implementation in how things are being done. One adapter implementation depends on the AdapterFactory and the Adapter itself. The factory will help the internal system to find the right adapter for the used repository method. Means you can use different adapters based on methods, types etc. These adapters can be added to the Repolizer class and will be stored as a list.
   - Wrapper Adapter: This adapter can be used to define the wrapper for the repository. You could write your own JavaRx wrapper so that your calls are wrapped into that.
   - Storage Adapter: That adapter is relevant for persisting your data. You could implement something with Room or even SharedPrefs if you prefer that.
   - Cache Adapter: That adapter will be used to keep track of your persistent data "age". The adapter saves the time when your data was downloaded and also helps to determine if the data should be refreshed.
    - Converter Adapter: This adapter is used by the storage adapter to convert any web stuff into real objects. You could have your own Gson Converter or even use something like Jackson. That's up to you.
    - Network Adapter: This adapter can be used to implement your own network logic. You could you Retrofit, okHttp or what ever you think is best.
- The app already provides you with several default adapter: GsonConveterAdapter, RetrofitNetworkAdapter, LiveDataWrapperAdapter, SharedPrefsCacheAdapter. The storage adapter is currently missing and is something I would like to do in the future. This adapter will probably use the missing Room implementation (and it's annotation processor).

Version 0.3.2 *(2018-08-01)*
----------------------------
- **BUGFIX:** Fixed UrlParameter processor implementation

Version 0.3.1 *(2018-07-10)*
----------------------------
- **NEW:** Added support for network queries that include non-unique queries ids, like https://myurl.com/myapi?id=1&id=2
- **BUGFIX:** Fixed annotation processor bug for the NetworkGetMethod that was wrongly initialising the CacheItem when the method url is empty.
- **MISC:** Updated kotlin to version 1.2.51.
- **MISC:** Updated room to version 1.1.1.

Version 0.3.0 *(2018-06-12)*
----------------------------

#### ANNOTATION

- **NEW:** Made the field 'name' for the Database annotation optionally. The default value is "database".
- **NEW:** Renamed field 'value' to 'typeConverter' for the TypeConverter annotation.
- **NEW:** Removed the field entityBodyAsList from the CUD annotation. The internal implementation of the CUD network builder will require a serializable object to simplify it's usage. Therefore if you use the CUD annotation, your annotated RequestBody object needs to implement serializable.
- **NEW:** Added a new field to the DB annotation which allows the user to define the onConflict strategy. This stategy will only be used for the database operation INSERT and UPDATE.
- **NEW:** Made the field 'url' for the GET annotation optionally. If empty the GET method will only return the current saved cache in the database.
- **NEW:** Made the field 'sql' for the DB annotation optionally.
- **NEW:** Renamed field 'sql' to 'querySql' for the GET annotation. If the field value is empty, the GET method will use the default sql query "SELECT * FROM tablename"
- **NEW:** Added new repository parameter which should handle the case if the cache is too old but the GET method was unable to retrieve new content (default will delete cache). The relevant value has been added to the Repository annotation. The value can also be set for each method by using the RepositoryParameter annotation (using the value 'DELETE_IF_CACHE_TOO_OLD').
- **NEW:** Added new field 'deleteSql' to the GET annotation. This field will define the optional sql query if the repository needs to delete something (like if the cache is too old). This event can be defined by using the new repository parameter (DELETE_IF_CACHE_TOO_OLD) and/or using the new field annotated inside the Repository annotations.
- **NEW:** Added onConflict stragtegy to the GET and REFRESH annotation.
- **NEW:** Added new Progress annotation which can be used for method parameter to define certain values for your ProgressController. This could include a title, message, color or what ever you feel like. The value for the Progress annotation needs to be an object which extends ProgressData. To this object you can assign the required progress values.

#### CORE
- **NEW**: Refactored most of the classes in the core artifact to avoid kotlin-NPE and clean up the implementations.
- **NEW:** Moved several classes to new/different packages.
- **NEW:** Refactored ProgressController. The class has now an internal map to ensure that the progress is only closed if no requests are running. Cancel is still working as expected. The new implementation is also responsible to handle the new Progress annotation. Therefore the abstract method onShow is getting the ProgressData object (when if none is set that will only include the RequestType). The ProgressController got a new method called resetController that should be used as soon as the user switches to a new actviity (or even fragment). Usually you don't want to block the user interaction if the progress is displayed. Therefore if the user is switching the context, the progress is not visible anymore and would block any if the progress that the new activity/fragment would show.
- **BUGFIX:** Changed the workerThread of the AppExecutor from newSingleThreadExecutor to newCachedThreadPool. This should increase the performance of your app significantly.
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

- **NEW**: Added some compiler messages for the usage of the DB annotation >> in case the DB annotation has been used without any parameter.
- **BUGFIX**: Added missing DatabaseBody implementation for the DB annotation (for INSERT, UPDATE, DELETE)
- **BUGFIX**: Added some conditions to make sure that the DB annotation is only using the relevant parameter. INSERT, UPDATE, DELETE are using the DatabaseBody annotation. QUERY is using the SqlParameter annotation.
- **MISC**: Updated Room to version 1.1.1-rc1. This version fixes some major crashes when using database migration.

Version 0.2.0 *(2018-06-01)*
----------------------------

- **NEW**: Changed the dependency of certain libraries which are used internally so that any other project does not need to implement those too.
- **NEW**: Added missing implementation so that the url query strings are included to the actual url for the caching.
- **NEW**: Moved prepareUrl from NetworkController to NetworkResource to simplify the usage of implementing custom NetworkController.
- **NEW**: Added support for different database operations when using the DB annotation. The annotation requires you to define a DatabaseOperation enum value. This value is used to create a certain method inside the dao which is used by this specific method. Database operations can be insert, update, query and delete.
- **BUGFIX**: Added some missing processor information.
- **BUGFIX**: Fixed prepareUrl that is used to remove all queries from the url.
- **MISC**: Decreased minimum SDK level from 16 to 14.
- **MISC**: Updated javapoet to version 1.11.1.

Version 0.1.0 *(2018-05-22)*
----------------------------

- Initial library release.
