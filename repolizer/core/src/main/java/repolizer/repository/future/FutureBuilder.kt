package repolizer.repository.future

import com.google.gson.reflect.TypeToken

abstract class FutureBuilder {
    var repositoryClass: Class<*>? = null
    var typeToken: TypeToken<*>? = null
    var bodyType: Class<*>? = null

    var url: String = ""
    var insertSql: String = ""
    var querySql: String = ""
    var updateSql: String = ""
    var deleteSql: String = ""
}