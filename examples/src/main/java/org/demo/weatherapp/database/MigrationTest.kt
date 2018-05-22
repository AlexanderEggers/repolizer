package org.demo.weatherapp.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

class MigrationTest: Migration(1, 2) {

    override fun migrate(database: SupportSQLiteDatabase) {
        //do nothing
    }
}