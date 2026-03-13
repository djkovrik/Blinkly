package com.sedsoftware.blinkly.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

@Suppress("FunctionName")
fun BlinklyDatabaseDriverFactory(): SqlDriver =
    NativeSqliteDriver(
        schema = BlinklyAppDatabase.Schema,
        name = "blinkly_database.db"
    )
