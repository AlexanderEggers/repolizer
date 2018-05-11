package org.demo.repolizer

import repolizer.annotation.repository.DB
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.SqlParameter

@Repository(entity = String::class, database = String::class, tableName = "TestTable")
interface DemoRepository {

    @DB("")
    fun test(@SqlParameter parameter: String)
}