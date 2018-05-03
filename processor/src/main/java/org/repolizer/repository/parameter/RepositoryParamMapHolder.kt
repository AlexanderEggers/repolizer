package org.repolizer.repository.parameter

import javax.lang.model.element.Element

object RepositoryParamMapHolder {

    val headerAnnotationMap: HashMap<String, Element>? = HashMap()
    val queryMapAnnotationMap: HashMap<String, Element>? = HashMap()
    val requestBodyAnnotationMap: HashMap<String, Element>? = HashMap()
    val sqlParameterAnnotationMap: HashMap<String, Element>? = HashMap()
    val urlParameterAnnotationMap: HashMap<String, Element>? = HashMap()
}