package org.demo.repolizer

import repolizer.annotation.database.Database
import repolizer.annotation.database.DatabaseType

@Database("DemoDatabase", 1, DatabaseType.PERSISTENT)
interface DemoDatabase