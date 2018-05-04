package repolizer.repository

import javax.lang.model.element.Element

object RepositoryMapHolder {

    val dbAnnotationMap: HashMap<String, Element> = HashMap()
    val deleteAnnotationMap: HashMap<String, Element> = HashMap()
    val getAnnotationMap: HashMap<String, Element> = HashMap()
    val postAnnotationMap: HashMap<String, Element> = HashMap()
    val putAnnotationMap: HashMap<String, Element> = HashMap()
    val refreshAnnotationMap: HashMap<String, Element> = HashMap()

    val databaseBodyAnnotationMap: HashMap<String, Element> = HashMap()
    val headerAnnotationMap: HashMap<String, Element> = HashMap()
    val requestBodyAnnotationMap: HashMap<String, Element> = HashMap()
    val sqlParameterAnnotationMap: HashMap<String, Element> = HashMap()
    val urlParameterAnnotationMap: HashMap<String, Element> = HashMap()
    val urlQueryAnnotationMap: HashMap<String, Element> = HashMap()
}