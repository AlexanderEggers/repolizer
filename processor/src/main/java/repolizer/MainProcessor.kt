package repolizer

import com.google.auto.service.AutoService
import repolizer.annotation.database.Database
import repolizer.annotation.database.Migration
import repolizer.annotation.database.TypeConverter
import repolizer.annotation.repository.*
import repolizer.annotation.repository.parameter.*
import repolizer.database.DatabaseMainProcessor
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
            DatabaseMainProcessor().process(this, roundEnv)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
                //Database annotations
                Database::class.java.name, Migration::class.java.name, TypeConverter::class.java.name,

                //Repository annotations
                Repository::class.java.name,
                //Repository method annotations
                REFRESH::class.java.name, GET::class.java.name, DB::class.java.name,
                CUD::class.java.name, CACHE::class.java.name,
                //Repository parameter annotations
                DatabaseBody::class.java.name, Header::class.java.name,
                RepositoryParameter::class.java.name, RequestBody::class.java.name,
                SqlParameter::class.java.name, UrlParameter::class.java.name,
                UrlQuery::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }
}