package com.sedsoftware.blinkly.component.reminders.integration

import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Interval
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Model
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.ReminderItem
import com.sedsoftware.blinkly.component.reminders.store.RemindersStore.State
import com.sedsoftware.blinkly.domain.model.Reminder
import com.sedsoftware.blinkly.domain.model.ReminderInterval
import com.sedsoftware.blinkly.domain.model.ReminderType

internal val stateToModel: (State) -> Model = { state ->
    Model(
        reminders = state.reminders
            .sortedWith(compareBy<Reminder> { it.date.date }.thenBy { it.date.time })
            .map(::reminderToItem),
        deletedReminder = state.deletedReminder?.let(::reminderToItem),
    )
}

private fun reminderToItem(reminder: Reminder): ReminderItem =
    ReminderItem(
        uuid = reminder.uuid,
        title = reminder.type.title,
        description = reminder.type.description,
        time = reminder.date.time,
        nextDate = reminder.date.date,
        interval = when (reminder.interval) {
            ReminderInterval.DAILY -> Interval.DAILY
            ReminderInterval.WEEKLY -> Interval.WEEKLY
        },
        days = reminder.weekDays,
    )

private val ReminderType.title: String
    get() = when (this) {
        ReminderType.TWENTY_X3 -> "20-20-20"
    }

private val ReminderType.description: String
    get() = when (this) {
        ReminderType.TWENTY_X3 -> "Look 20 feet away for 20 seconds"
    }
