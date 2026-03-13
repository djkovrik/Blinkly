package com.sedsoftware.blinkly.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlin.time.Instant

internal object InstantAdapter : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant {
        return Instant.parse(databaseValue)
    }

    override fun encode(value: Instant): String {
        return value.toString()
    }
}
