package repolizer.database

import com.squareup.javapoet.ClassName
import javax.lang.model.element.Element

object DatabaseMapHolder {
    val daoMap: HashMap<String, ArrayList<ClassName>> = HashMap()
    val entityMap: HashMap<String, ArrayList<ClassName>> = HashMap()

    val migrationAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
}