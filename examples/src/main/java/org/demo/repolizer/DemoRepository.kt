package org.demo.repolizer

import org.repolizer.annotation.repository.DB
import org.repolizer.annotation.repository.Repository
import org.repolizer.annotation.repository.method.SQLParameter

@Repository(entity = String::class, database = String::class)
interface DemoRepository {

    @DB("")
    fun test(@SQLParameter parameter: String)
}