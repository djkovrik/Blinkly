package com.sedsoftware.blinkly.domain.model

import kotlinx.datetime.LocalDateTime

data class Reminder(
    val uuid: String,
    val scheduleId: String,
    val date: LocalDateTime,
    val type: ReminderType,
    val interval: ReminderInterval,
)
