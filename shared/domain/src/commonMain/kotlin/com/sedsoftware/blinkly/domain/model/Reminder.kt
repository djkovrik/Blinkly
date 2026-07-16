package com.sedsoftware.blinkly.domain.model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

data class Reminder(
    val uuid: String,
    val scheduleId: String,
    val date: LocalDateTime,
    val type: ReminderType,
    val interval: ReminderInterval,
)

fun Reminder.nextOccurrence(now: Instant, timeZone: TimeZone): LocalDateTime {
    val currentLocal = now.toLocalDateTime(timeZone)
    val daysToAdd = when (interval) {
        ReminderInterval.DAILY -> 0
        ReminderInterval.WEEKLY ->
            (date.dayOfWeek.ordinal - currentLocal.dayOfWeek.ordinal + FULL_WEEK_DAYS) % FULL_WEEK_DAYS
    }
    val candidate = LocalDateTime(
        date = currentLocal.date.plus(daysToAdd.toLong(), DateTimeUnit.DAY),
        time = date.time,
    )

    if (candidate.toInstant(timeZone) > now) return candidate

    val intervalDays = when (interval) {
        ReminderInterval.DAILY -> 1L
        ReminderInterval.WEEKLY -> FULL_WEEK_DAYS.toLong()
    }
    return LocalDateTime(
        date = candidate.date.plus(intervalDays, DateTimeUnit.DAY),
        time = candidate.time,
    )
}

private const val FULL_WEEK_DAYS = 7
