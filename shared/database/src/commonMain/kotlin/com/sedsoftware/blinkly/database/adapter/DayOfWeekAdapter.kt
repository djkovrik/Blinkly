package com.sedsoftware.blinkly.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.json.Json

internal object DayOfWeekAdapter : ColumnAdapter<List<DayOfWeek>, String> {

    private val json: Json by lazy {
        Json {
            isLenient = true
        }
    }

    override fun decode(databaseValue: String): List<DayOfWeek> {
        return json.decodeFromString(databaseValue)
    }

    override fun encode(value: List<DayOfWeek>): String {
        return json.encodeToString(value)
    }
}
