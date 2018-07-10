package repolizer.annotation.repository.util

enum class DatabaseOperation(val objectExpected: Boolean) {
    INSERT(true),
    UPDATE(true),
    DELETE(true)
}