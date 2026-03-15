package com.sedsoftware.blinkly.database.di

import app.cash.sqldelight.db.SqlDriver
import com.sedsoftware.blinkly.database.impl.BlinklyDatabaseImpl
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils

interface DatabaseModule {
    val database: BlinklyDatabase
}

interface DatabaseModuleDependencies {
    val driver: SqlDriver
    val dispatchers: BlinklyDispatchers
    val timeUtils: BlinklyTimeUtils
}

fun DatabaseModule(dependencies: DatabaseModuleDependencies): DatabaseModule {
    return object : DatabaseModule {
        override val database: BlinklyDatabase by lazy {
            BlinklyDatabaseImpl(
                dispatchers = dependencies.dispatchers,
                driver = dependencies.driver,
                timeUtils = dependencies.timeUtils,
            )
        }
    }
}
