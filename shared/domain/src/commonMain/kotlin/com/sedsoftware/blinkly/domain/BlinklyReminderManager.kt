package com.sedsoftware.blinkly.domain

import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ScheduledReminder
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

interface BlinklyReminderManager {
    fun createdReminders(): Flow<List<Reminder>>
    fun createdSchedules(): Flow<List<ScheduledReminder>>

    suspend fun scheduleDaily(time: LocalTime)
    suspend fun scheduleWeeklySingle(time: LocalTime, dayOfWeek: DayOfWeek)
    suspend fun scheduleWeeklyDayPeriod(from: LocalTime, until: LocalTime, intervalMinutes: Int, days: List<DayOfWeek>)
    suspend fun rescheduleAll()
    suspend fun cancelSchedule(scheduleId: String)
    suspend fun cancelAll()
}
