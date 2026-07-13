package com.sedsoftware.blinkly.component.reminders.domain

import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ScheduledReminder
import kotlinx.coroutines.flow.Flow

internal class RemindersManager(
    private val reminderManager: BlinklyReminderManager,
    private val notifier: BlinklyNotifier,
) {

    fun observeReminders(): Flow<List<ScheduledReminder>> =
        reminderManager.createdSchedules()

    fun observePermissionEvents(): Flow<PermissionResult> =
        notifier.permissionEvents()

    suspend fun isNotificationPermissionGranted(): Result<Boolean> =
        runCatching {
            notifier.isNotificationPermissionGranted()
        }

    suspend fun requestNotificationPermission(): Result<Unit> =
        runCatching {
            notifier.requestNotificationPermission()
        }

    suspend fun deleteReminder(scheduleId: String): Result<Unit> =
        runCatching {
            reminderManager.cancelSchedule(scheduleId)
        }

    suspend fun restoreReminder(reminder: ScheduledReminder): Result<Unit> =
        runCatching {
            when (val configuration = reminder.schedule.configuration) {
                is ReminderScheduleConfiguration.Daily ->
                    reminderManager.scheduleDaily(configuration.time)
                is ReminderScheduleConfiguration.WeeklySingle ->
                    reminderManager.scheduleWeeklySingle(configuration.time, configuration.day)
                is ReminderScheduleConfiguration.WorkdayPeriod ->
                    reminderManager.scheduleWeeklyDayPeriod(
                        from = configuration.from,
                        until = configuration.until,
                        intervalMinutes = configuration.intervalMinutes,
                        days = configuration.days,
                    )
            }
        }
}
