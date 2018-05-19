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

@Repository(entity = String::class, database = String::class, tableName = "TestTable")
interface DemoRepository {

    @REFRESH("")
    fun testRefresh(): LiveData<String>

    @GET("")
    fun testGet(@RepositoryParameter(ParameterType.ALLOW_FETCH) allowFetch: Boolean): LiveData<List<String>>

    @CUD("", CudType.POST)
    fun testPost(@RequestBody myBody: String): LiveData<String>

    @CUD("", CudType.PUT)
    fun testPut(@RequestBody myBody: String): LiveData<String>

    @CUD("", CudType.DELETE)
    fun testDelete(@RequestBody myBody: String): LiveData<String>

    @DB("")
    fun test(@SqlParameter parameter: String)

    @CACHE(CacheOperation.INSERT)
    fun cacheTest(@DatabaseBody url: String)
}