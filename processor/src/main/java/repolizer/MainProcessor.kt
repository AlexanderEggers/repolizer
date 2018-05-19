package repolizer

import com.google.auto.service.AutoService
import repolizer.annotation.repository.DB
import repolizer.annotation.repository.Repository
import repolizer.annotation.repository.parameter.Header
import repolizer.repository.RepositoryMainProcessor
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@AutoService(Processor::class)
class MainProcessor : AbstractProcessor() {

    lateinit var filer: Filer
    lateinit var messager: Messager
    lateinit var elements: Elements

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        filer = processingEnvironment.filer
        messager = processingEnvironment.messager
        elements = processingEnvironment.elementUtils
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            RepositoryMainProcessor().process(this, roundEnv)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Repository::class.java.name, DB::class.java.name, Header::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }
}