package com.sedsoftware.blinkly.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

@Suppress("FunctionName")
fun BlinklyDatabaseDriverFactory(): SqlDriver {
    val databasePath = File(System.getProperty("java.io.tmpdir"), "blinkly_database.db")
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
    BlinklyAppDatabase.Schema.create(driver)
    return driver
}
