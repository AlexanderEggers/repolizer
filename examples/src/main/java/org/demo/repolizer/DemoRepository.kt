package org.demo.repolizer

import android.arch.lifecycle.LiveData
import repolizer.annotation.repository.DB
import repolizer.annotation.repository.GET
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.SqlParameter

@Repository(entity = String::class, database = String::class, tableName = "TestTable")
interface DemoRepository {

    @DB("")
    fun test(@SqlParameter parameter: String)

    @GET("")
    fun testGet(): LiveData<List<String>>
}