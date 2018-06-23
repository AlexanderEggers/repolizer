package repolizer.database

import com.squareup.javapoet.ClassName

class DatabaseProcessorUtil {

    companion object {
        fun addClassNameToDatabaseHolderMap(hashMap: HashMap<String, ArrayList<ClassName>>,
                                            databaseName: String, daoName: ClassName) {
            val currentList: ArrayList<ClassName> = hashMap[databaseName] ?: ArrayList()
            currentList.add(daoName)
            hashMap[databaseName] = currentList
        }
    }
}
