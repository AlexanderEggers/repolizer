package repolizer.annotation.repository

import repolizer.annotation.repository.util.CacheOperation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CACHE(val operation: CacheOperation)