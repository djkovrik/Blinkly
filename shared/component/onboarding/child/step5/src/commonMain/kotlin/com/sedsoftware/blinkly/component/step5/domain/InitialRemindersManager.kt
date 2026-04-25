package com.sedsoftware.blinkly.component.step5.domain

import com.sedsoftware.blinkly.component.step5.store.InitialRemindersStore
import com.sedsoftware.blinkly.domain.BlinklyReminderManager
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.model.PermissionResult
import com.sedsoftware.blinkly.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

internal class InitialRemindersManager(
    private val reminderManager: BlinklyReminderManager,
    private val notifier: BlinklyNotifier
) {

    fun observeReminders(): Flow<List<Reminder>> =
        reminderManager.createdReminders()

    fun observePermissionEvents(): Flow<PermissionResult> =
        notifier.permissionEvents()

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

    suspend fun isNotificationsPermissionGranted(): Result<Boolean> =
        runCatching {
            notifier.isNotificationPermissionGranted()
        }

    suspend fun requestNotificationsPermission(): Result<Unit> =
        runCatching {
            notifier.requestNotificationPermission()
        }
}
