package com.sedsoftware.blinkly.component.newreminder.domain

import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal class AddNewReminderManager(
    private val reminderManager: BlinklyReminderManager,
) {

    suspend fun scheduleDaily(time: LocalTime): Result<Unit> =
        runCatching {
            reminderManager.scheduleDaily(time)
        }

    suspend fun scheduleWeeklySingle(time: LocalTime, dayOfWeek: DayOfWeek): Result<Unit> =
        runCatching {
            reminderManager.scheduleWeeklySingle(time, dayOfWeek)
        }

    suspend fun scheduleWeeklyDayPeriod(
        from: LocalTime,
        until: LocalTime,
        intervalMinutes: Int,
        days: List<DayOfWeek>,
    ): Result<Unit> =
        runCatching {
            reminderManager.scheduleWeeklyDayPeriod(
                from = from,
                until = until,
                intervalMinutes = intervalMinutes,
                days = days,
            )
        }
}
