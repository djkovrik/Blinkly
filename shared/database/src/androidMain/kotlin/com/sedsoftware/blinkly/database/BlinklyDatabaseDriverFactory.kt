package com.sedsoftware.blinkly.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

@Suppress("FunctionName")
fun BlinklyDatabaseDriverFactory(context: Context): SqlDriver =
    AndroidSqliteDriver(
        schema = BlinklyAppDatabase.Schema,
        context = context,
        name = "blinkly_database.db",
    )
