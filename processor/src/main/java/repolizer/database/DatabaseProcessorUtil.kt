package repolizer.database

import com.squareup.javapoet.ClassName

class DatabaseProcessorUtil {

    companion object {
        fun addClassNameToDatabaseMap(hashMap: HashMap<String, ArrayList<ClassName>>, databaseName: String,
                                daoName: ClassName) {
            var currentList: ArrayList<ClassName>? = hashMap[databaseName]
            if (currentList == null) {
                currentList = ArrayList()
            }
            currentList.add(daoName)
            hashMap[databaseName] = currentList
        }
    }
}
