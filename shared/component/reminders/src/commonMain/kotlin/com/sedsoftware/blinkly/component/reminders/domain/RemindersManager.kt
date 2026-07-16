package com.sedsoftware.blinkly.component.reminders.domain

import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.domain.model.ReminderScheduleConfiguration
import com.sedsoftware.blinkly.domain.model.ScheduledReminder
import com.sedsoftware.blinkly.domain.model.nextOccurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RemindersManager(
    private val reminderManager: BlinklyReminderManager,
    private val notifier: BlinklyNotifier,
    private val timeUtils: BlinklyTimeUtils,
) {

    fun observeReminders(): Flow<List<ScheduledReminder>> =
        reminderManager.createdSchedules().map(::withNextOccurrences)

    fun refreshReminders(reminders: List<ScheduledReminder>): Result<List<ScheduledReminder>> =
        runCatching {
            withNextOccurrences(reminders)
        }

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

    fun prepareExactAlarmPermission(): Result<ExactAlarmPermissionResult> =
        runCatching {
            if (reminderManager.canScheduleExactAlarms()) {
                ExactAlarmPermissionResult.Granted
            } else {
                reminderManager.requestExactAlarmPermission()
                ExactAlarmPermissionResult.Requested
            }
        }

    fun isExactAlarmPermissionGranted(): Result<Boolean> =
        runCatching {
            reminderManager.canScheduleExactAlarms()
        }

    suspend fun rescheduleAll(): Result<Unit> =
        runCatching {
            reminderManager.rescheduleAll()
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

    private fun withNextOccurrences(reminders: List<ScheduledReminder>): List<ScheduledReminder> {
        val now = timeUtils.now()
        val timeZone = timeUtils.timeZone()

        return reminders.map { scheduledReminder ->
            scheduledReminder.copy(
                alarms = scheduledReminder.alarms.map { reminder ->
                    reminder.copy(date = reminder.nextOccurrence(now, timeZone))
                }
            )
        }
    }

    enum class ExactAlarmPermissionResult {
        Granted,
        Requested,
    }
}
