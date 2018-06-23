package repolizer.annotation.repository.parameter

import repolizer.annotation.repository.util.ParameterType

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class RepositoryParameter(val type: ParameterType)