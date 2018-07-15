package repolizer.annotation.repository

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Repository(val allowFetchByDefault: Boolean = true,
                            val deleteIfCacheIsTooOld: Boolean = true,
                            val allowMultipleRequestsAtSameTime: Boolean = false)