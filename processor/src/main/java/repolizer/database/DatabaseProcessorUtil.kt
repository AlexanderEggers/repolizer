package repolizer.database

import com.squareup.javapoet.ClassName

class DatabaseProcessorUtil {

    companion object {
        fun addDaoToDatabaseMap(databaseName: String, daoName: ClassName) {
            val hashMap = DatabaseMapHolder.daoMap

            var currentList: ArrayList<ClassName>? = hashMap[databaseName]
            if (currentList == null) {
                currentList = ArrayList()
            }
            currentList.add(daoName)
            hashMap[databaseName] = currentList
        }
    }
}
