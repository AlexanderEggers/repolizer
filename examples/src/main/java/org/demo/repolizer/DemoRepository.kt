package org.demo.repolizer

import org.repolizer.annotation.repository.DB
import org.repolizer.annotation.repository.Repository
import org.repolizer.annotation.repository.parameter.Header

@Repository(entity = String::class, database = String::class)
interface DemoRepository {

    @DB("")
    fun test(@Header("test") parameter: String)
}