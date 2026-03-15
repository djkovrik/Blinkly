package com.sedsoftware.blinkly.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime

data class Reminder(
    val uuid: String,
    val date: LocalDateTime,
    val type: ReminderType,
    val interval: ReminderInterval,
    val weekDays: List<DayOfWeek>,
)
