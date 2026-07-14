package com.sedsoftware.blinkly.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalTime

internal object LocalTimeAdapter : ColumnAdapter<LocalTime, String> {
    override fun decode(databaseValue: String): LocalTime =
        LocalTime.parse(databaseValue)

    override fun encode(value: LocalTime): String =
        value.toString()
}
