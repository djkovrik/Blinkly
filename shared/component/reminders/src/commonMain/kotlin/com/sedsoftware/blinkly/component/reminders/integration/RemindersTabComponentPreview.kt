package com.sedsoftware.blinkly.component.reminders.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Interval
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Model
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.ReminderItem
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class RemindersTabComponentPreview(
    reminders: List<ReminderItem> = defaultReminders,
    deletedReminder: ReminderItem? = null,
) : RemindersTabComponent {

    override val model: Value<Model> =
        MutableValue(
            Model(
                reminders = reminders,
                deletedReminder = deletedReminder,
            )
        )

    override fun onAddNewClick() = Unit
    override fun onDeleteReminder(uuid: String) = Unit
    override fun onUndoDelete() = Unit
    override fun onDeletedMessageShown() = Unit

    companion object {
        val defaultReminders: List<ReminderItem> =
            listOf(
                ReminderItem(
                    uuid = "daily",
                    title = "20-20-20",
                    description = "Look 20 feet away for 20 seconds",
                    time = LocalTime(hour = 10, minute = 0),
                    nextDate = LocalDate(year = 2026, month = 6, day = 20),
                    interval = Interval.DAILY,
                    days = emptyList(),
                ),
                ReminderItem(
                    uuid = "weekly",
                    title = "20-20-20",
                    description = "Look 20 feet away for 20 seconds",
                    time = LocalTime(hour = 14, minute = 30),
                    nextDate = LocalDate(year = 2026, month = 6, day = 22),
                    interval = Interval.WEEKLY,
                    days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                ),
            )
    }
}
