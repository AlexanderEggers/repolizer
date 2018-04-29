package org.repolizer.util

import com.squareup.javapoet.ClassName
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

class ProcessorUtil {

    companion object {

        fun classAndroidInjector(): ClassName {
            return ClassName.get("dagger.android", "ContributesAndroidInjector")
        }

        fun classModule(): ClassName {
            return ClassName.get("dagger", "Module")
        }

        fun classViewModel(): ClassName {
            return ClassName.get("android.arch.lifecycle", "ViewModel")
        }
    }
}