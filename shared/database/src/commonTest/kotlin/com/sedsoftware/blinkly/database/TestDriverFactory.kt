package com.sedsoftware.blinkly.database

import app.cash.sqldelight.db.SqlDriver

@Suppress("FunctionName")
expect fun TestDriverFactory(): SqlDriver
