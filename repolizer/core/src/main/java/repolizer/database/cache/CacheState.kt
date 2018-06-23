package repolizer.database.cache

enum class CacheState {
    NEEDS_SOFT_REFRESH,
    NEEDS_HARD_REFRESH,
    NEEDS_NO_REFRESH,
    NO_CACHE
}