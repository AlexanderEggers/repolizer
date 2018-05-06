package org.demo.repolizer

import repolizer.annotation.repository.DB
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.Header

@Repository(entity = String::class, database = String::class, tableName = "TestTable")
interface DemoRepository {

    @DB("")
    fun test(@Header("test") parameter: String)
}