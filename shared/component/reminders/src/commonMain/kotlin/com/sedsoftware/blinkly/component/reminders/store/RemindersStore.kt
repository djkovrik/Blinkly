package com.sedsoftware.blinkly.component.reminders.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.Intent
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.Label
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.State
import com.sedsoftware.blinkly.domain.model.ScheduledReminder

internal interface RemindersStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object AddNewReminder : Intent
        data object AppResumed : Intent
        data class DeleteReminder(val scheduleId: String) : Intent
        data object UndoDelete : Intent
        data object DeleteMessageShown : Intent
    }

    data class State(
        val reminders: List<ScheduledReminder> = emptyList(),
        val deletedReminder: ScheduledReminder? = null,
        val pendingDeleteScheduleId: String? = null,
        val isRestoringDeleted: Boolean = false,
        val isRequestingNotificationPermission: Boolean = false,
        val isAwaitingExactAlarmPermission: Boolean = false,
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
        data object OpenAddNewReminder : Label()
    }
}
