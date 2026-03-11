package com.sedsoftware.blinkly.domain.extension

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

val Instant.hour: Int
    get() = toLocalDateTime(TimeZone.currentSystemDefault()).hour

val Instant.minute: Int
    get() = toLocalDateTime(TimeZone.currentSystemDefault()).minute
