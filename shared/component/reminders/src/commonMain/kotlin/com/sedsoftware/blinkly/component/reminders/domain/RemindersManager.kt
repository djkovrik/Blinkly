package com.sedsoftware.blinkly.component.reminders.domain

import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import kotlinx.coroutines.flow.Flow

internal class RemindersManager(
    private val reminderManager: BlinklyReminderManager,
    private val notifier: BlinklyNotifier,
) {

    fun observeReminders(): Flow<List<Reminder>> =
        reminderManager.createdReminders()

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

    suspend fun deleteReminder(uuid: String): Result<Unit> =
        runCatching {
            reminderManager.cancel(uuid)
        }

    suspend fun restoreReminder(reminder: Reminder): Result<Unit> =
        runCatching {
            when (reminder.interval) {
                ReminderInterval.DAILY -> reminderManager.scheduleDaily(reminder.date.time)
                ReminderInterval.WEEKLY -> {
                    val dayOfWeek = reminder.weekDays.firstOrNull() ?: reminder.date.dayOfWeek
                    reminderManager.scheduleWeeklySingle(reminder.date.time, dayOfWeek)
                }
            }
        }
}
