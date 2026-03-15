package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.ReminderType
import kotlinx.datetime.LocalDateTime

interface BlinklyAlarmManager {
    fun scheduleDaily(uuid: String, type: ReminderType, startingDate: LocalDateTime)
    fun scheduleWeekly(uuid: String, type: ReminderType, startingDate: LocalDateTime)
    fun cancel(uuid: String)
    fun cancelAll()
}
