package org.repolizer

import com.google.auto.service.AutoService
import org.repolizer.annotation.RepositoryProcessor
import org.repolizer.annotation.repository.DB
import org.repolizer.annotation.repository.Repository
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@AutoService(Processor::class)
class MainProcessor : AbstractProcessor() {

    var filer: Filer? = null
    var messager: Messager? = null
    var elements: Elements? = null

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        filer = processingEnvironment.filer
        messager = processingEnvironment.messager
        elements = processingEnvironment.elementUtils
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            RepositoryProcessor().process(this, roundEnv)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Repository::class.java.name, DB::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }
}