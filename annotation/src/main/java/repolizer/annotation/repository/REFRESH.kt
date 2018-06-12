package repolizer.annotation.repository

import repolizer.annotation.repository.util.OnConflictStrategy

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class REFRESH(val url: String,
                         val requiresLogin: Boolean = false,
                         val showProgress: Boolean = false,
                         val getAsList: Boolean = true,
                         val onConflictStrategy: OnConflictStrategy = OnConflictStrategy.REPLACE)