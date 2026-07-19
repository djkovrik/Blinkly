package com.sedsoftware.blinkly.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

internal object DayOfWeekAdapter : ColumnAdapter<List<DayOfWeek>, String> {

    private val json: Json by lazy {
        Json {
            isLenient = true
        }
    }

    private val serializer = ListSerializer(String.serializer())

    override fun decode(databaseValue: String): List<DayOfWeek> {
        return json.decodeFromString(serializer, databaseValue)
            .map(DayOfWeek::valueOf)
    }

    override fun encode(value: List<DayOfWeek>): String {
        return json.encodeToString(serializer, value.map(DayOfWeek::name))
    }
}
