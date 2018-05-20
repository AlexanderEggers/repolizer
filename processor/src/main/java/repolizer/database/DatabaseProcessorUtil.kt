package repolizer.database

import javax.lang.model.element.Element

class DatabaseProcessorUtil {

    companion object {
        fun addDaoToDatabaseMap(databaseName: String, daoName: String) {
            val hashMap = DatabaseMapHolder.daoMap

            var currentList: ArrayList<String>? = hashMap[databaseName]
            if (currentList == null) {
                currentList = ArrayList()
            }
            currentList.add(daoName)
            hashMap[databaseName] = currentList
        }
    }
}
