package repolizer.adapter

import repolizer.repository.future.Future
import java.lang.reflect.Type

interface WrapperAdapter<O> {

    fun getType(): Type

    fun <B> execute(future: Future<O, B>): O
}