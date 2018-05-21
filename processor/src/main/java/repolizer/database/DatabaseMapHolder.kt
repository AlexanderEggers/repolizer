package repolizer.database

import com.squareup.javapoet.ClassName

object DatabaseMapHolder {
    val daoMap: HashMap<String, ArrayList<ClassName>> = HashMap()
    val entityMap: HashMap<String, ArrayList<ClassName>> = HashMap()
}