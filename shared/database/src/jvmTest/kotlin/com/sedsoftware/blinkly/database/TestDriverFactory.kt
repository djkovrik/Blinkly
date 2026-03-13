package com.sedsoftware.blinkly.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

@Suppress("FunctionName")
actual fun TestDriverFactory(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { driver ->
        BlinklyAppDatabase.Schema.create(driver)
    }
