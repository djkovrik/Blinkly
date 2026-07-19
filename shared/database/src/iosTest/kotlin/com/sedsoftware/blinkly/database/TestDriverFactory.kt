package com.sedsoftware.blinkly.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver

@Suppress("FunctionName")
actual fun TestDriverFactory(): SqlDriver =
    inMemoryDriver(BlinklyAppDatabase.Schema)
