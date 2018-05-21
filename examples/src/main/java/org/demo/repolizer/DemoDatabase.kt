package org.demo.repolizer

import repolizer.annotation.database.Database
import repolizer.annotation.database.util.DatabaseType

@Database("DemoDatabase", 1, DatabaseType.PERSISTENT)
interface DemoDatabase