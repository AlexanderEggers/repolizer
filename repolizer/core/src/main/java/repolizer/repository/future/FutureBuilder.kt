package repolizer.repository.future

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

abstract class FutureBuilder {
    var repositoryClass: Class<*>? = null
    var typeToken: TypeToken<*>? = null
    var bodyType: Type? = null

    var url: String = ""
    var insertSql: String = ""
    var querySql: String = ""
    var updateSql: String = ""
    var deleteSql: String = ""
}