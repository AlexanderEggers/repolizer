package repolizer.repository.future

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

abstract class FutureRequest(builder: FutureRequestBuilder) {
    val repositoryClass: Class<*> = builder.repositoryClass
            ?: throw IllegalStateException("Repository class type is null.")
    val typeToken: TypeToken<*> = builder.typeToken
            ?: throw IllegalStateException("Wrapper type is null.")
    val bodyType: Type = builder.bodyType
            ?: throw IllegalStateException("Body type is null.")

    val url: String = builder.url
    val insertStatement: String = builder.insertStatement
    val queryStatement: String = builder.queryStatement
    val updateStatement: String = builder.updateStatement
    val deleteStatement: String = builder.deleteStatement
}