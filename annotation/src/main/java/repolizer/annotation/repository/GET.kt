package repolizer.annotation.repository

import repolizer.annotation.repository.util.OnConflictStrategy

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class GET(val url: String = "",
                     val getAsList: Boolean = true,
                     val querySql: String = "",
                     val deleteSql: String = "",
                     val maxCacheTime: Long = Long.MAX_VALUE,
                     val maxFreshTime: Long = Long.MAX_VALUE,
                     val requiresLogin: Boolean = false,
                     val showProgress: Boolean = false,
                     val onConflictStrategy: OnConflictStrategy = OnConflictStrategy.REPLACE)