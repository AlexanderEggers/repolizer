package repolizer.repository

import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

object RepositoryMapHolder {

    val cacheAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val dbAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val cudAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val getAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val refreshAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()

    val databaseBodyAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val repositoryParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val headerAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val requestBodyAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val sqlParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val urlParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val urlQueryAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val progressParamsAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
}