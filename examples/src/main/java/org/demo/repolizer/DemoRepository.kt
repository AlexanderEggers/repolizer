package org.demo.repolizer

import android.arch.lifecycle.LiveData
import repolizer.annotation.repository.*
import repolizer.annotation.repository.parameter.DatabaseBody
import repolizer.annotation.repository.parameter.RepositoryParameter
import repolizer.annotation.repository.parameter.RequestBody
import repolizer.annotation.repository.parameter.SqlParameter
import repolizer.annotation.repository.util.CacheOperation
import repolizer.annotation.repository.util.CudType
import repolizer.annotation.repository.util.ParameterType
import repolizer.database.cache.CacheItem

@Repository(entity = DemoEntity::class, database = DemoDatabase::class, tableName = "demo_table")
interface DemoRepository {

    @REFRESH("")
    fun testRefresh(): LiveData<String>

    @GET("")
    fun testGet(@RepositoryParameter(ParameterType.ALLOW_FETCH) allowFetch: Boolean): LiveData<List<DemoEntity>>

    @CUD("", CudType.POST)
    fun testPost(@RequestBody myBody: String): LiveData<String>

    @CUD("", CudType.PUT)
    fun testPut(@RequestBody myBody: String): LiveData<String>

    @CUD("", CudType.DELETE)
    fun testDelete(@RequestBody myBody: String): LiveData<String>

    @DB("")
    fun testDB(@SqlParameter parameter: String)

    @CACHE(CacheOperation.INSERT)
    fun cacheTest(@DatabaseBody cacheItem: CacheItem)
}