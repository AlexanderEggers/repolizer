package repolizer.database

import com.squareup.javapoet.ClassName
import javax.lang.model.element.Element

object DatabaseMapHolder {

    val daoMap: HashMap<String, ArrayList<ClassName>> = HashMap()

    val converterAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val migrationAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
}