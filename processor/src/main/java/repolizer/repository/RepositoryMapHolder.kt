package repolizer.repository

import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement

object RepositoryMapHolder {

    val dbAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val deleteAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val getAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val postAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val putAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()
    val refreshAnnotationMap: HashMap<String, ArrayList<ExecutableElement>> = HashMap()

    val repositoryParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val headerAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val requestBodyAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val sqlParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val urlParameterAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
    val urlQueryAnnotationMap: HashMap<String, ArrayList<VariableElement>> = HashMap()
}