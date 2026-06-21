package com.sedsoftware.blinkly.component.reminders.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.Intent
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.Label
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.State
import com.sedsoftware.blinkly.domain.model.Reminder

internal interface RemindersStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class DeleteReminder(val uuid: String) : Intent
        data object UndoDelete : Intent
        data object DeleteMessageShown : Intent
    }

    data class State(
        val reminders: List<Reminder> = emptyList(),
        val deletedReminder: Reminder? = null,
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }
}
