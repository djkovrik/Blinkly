package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

interface BlinklyReminderManager {
    val reminders: Flow<List<Reminder>>

    suspend fun scheduleDaily(time: LocalTime)
    suspend fun scheduleWeeklySingle(time: LocalTime, dayOfWeek: DayOfWeek)
    suspend fun scheduleWeeklyDayPeriod(from: LocalTime, until: LocalTime, intervalMinutes: Int, days: List<DayOfWeek>)
    suspend fun rescheduleAll()
    suspend fun cancel(uuid: String)
    suspend fun cancelAll()
}
