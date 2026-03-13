package com.sedsoftware.blinkly.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

@Suppress("FunctionName")
actual fun TestDriverFactory(): SqlDriver =
    NativeSqliteDriver(
        schema = BlinklyAppDatabase.Schema,
        name = ":memory:",
    )
