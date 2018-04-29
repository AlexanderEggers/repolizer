package org.repolizer.annotation

import org.repolizer.MainProcessor
import org.repolizer.annotation.repository.Repository
import org.repolizer.util.AnnotationProcessor
import javax.annotation.processing.RoundEnvironment

class RepositoryProcessor : AnnotationProcessor {

    override fun process(mainProcessor: MainProcessor, roundEnv: RoundEnvironment) {
        roundEnv.getElementsAnnotatedWith(Repository::class.java).forEach {

        }
    }
}