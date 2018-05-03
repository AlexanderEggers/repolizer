package org.repolizer.repository.method

import javax.lang.model.element.Element

object RepositoryMethodMapHolder {

    val dbAnnotationMap: HashMap<String, Element>? = HashMap()
    val deleteAnnotationMap: HashMap<String, Element>? = HashMap()
    val getAnnotationMap: HashMap<String, Element>? = HashMap()
    val postAnnotationMap: HashMap<String, Element>? = HashMap()
    val putAnnotationMap: HashMap<String, Element>? = HashMap()
    val refreshAnnotationMap: HashMap<String, Element>? = HashMap()
}