package com.sedsoftware.blinkly.component.step5.domain

import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

internal class InitialRemindersManager(
    private val reminderManager: BlinklyReminderManager,
) {

    fun observeReminders(): Flow<List<Reminder>> = reminderManager.reminders

    suspend fun setupInitial(state: InitialRemindersStore.State) : Result<Unit> =
        runCatching {
            reminderManager.scheduleWeeklyDayPeriod(
                from = state.remindFrom,
                until = state.remindUntil,
                intervalMinutes = state.remindIntervalMinutes,
                days = state.selectedDays,
            )
        }

    suspend fun clearInitial(): Result<Unit> = runCatching {
        reminderManager.cancelAll()
    }
}
