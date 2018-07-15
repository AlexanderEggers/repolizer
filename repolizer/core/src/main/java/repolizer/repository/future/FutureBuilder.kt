package repolizer.repository.future

abstract class FutureBuilder {
    var repositoryClass: Class<*>? = null

    var url: String = ""
    var insertSql: String = ""
    var querySql: String = ""
    var updateSql: String = ""
    var deleteSql: String = ""
}