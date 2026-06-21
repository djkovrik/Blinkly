package com.sedsoftware.blinkly.component.reminders.domain

import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import kotlinx.coroutines.flow.Flow

internal class RemindersManager(
    private val reminderManager: BlinklyReminderManager,
) {

    fun observeReminders(): Flow<List<Reminder>> =
        reminderManager.createdReminders()

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
