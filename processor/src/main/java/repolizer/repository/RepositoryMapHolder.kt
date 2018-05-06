package repolizer.repository

import javax.lang.model.element.Element

object RepositoryMapHolder {

    val dbAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val deleteAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val getAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val postAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val putAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val refreshAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()

    val repositoryParameterAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val databaseBodyAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val headerAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val requestBodyAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val sqlParameterAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val urlParameterAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
    val urlQueryAnnotationMap: HashMap<String, ArrayList<Element>> = HashMap()
}