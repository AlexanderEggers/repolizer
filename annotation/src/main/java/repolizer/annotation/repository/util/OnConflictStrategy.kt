package repolizer.annotation.repository.util

enum class OnConflictStrategy {
    REPLACE,
    ROLLBACK,
    ABORT,
    FAIL,
    IGNORE
}