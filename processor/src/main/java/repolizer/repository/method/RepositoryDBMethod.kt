package repolizer.repository.method

import com.squareup.javapoet.MethodSpec
import javax.lang.model.element.Element

class RepositoryDBMethod constructor(private val methodBuilder: MethodSpec.Builder) {

    fun build(element: Element): MethodSpec.Builder {





        return methodBuilder
    }
}