package com.sedsoftware.blinkly.domain.extension

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.hour(timeZone: TimeZone): Int =
    toLocalDateTime(timeZone).hour

fun Instant.minute(timeZone: TimeZone): Int =
    toLocalDateTime(timeZone).minute

fun Instant.asLocalDate(timeZone: TimeZone): LocalDate =
    toLocalDateTime(timeZone).date
