package repolizer.repository.future

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

abstract class FutureRequestBuilder {
    var repositoryClass: Class<*>? = null
    var typeToken: TypeToken<*>? = null
    var bodyType: Type? = null

    var url: String = ""
    var insertStatement: String = ""
    var queryStatement: String = ""
    var updateStatement: String = ""
    var deleteStatement: String = ""
}