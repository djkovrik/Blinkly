package com.sedsoftware.blinkly.component.reminders.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Model
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.ReminderItem
import com.sedsoftware.blinkly.component.reminders.RemindersTabComponent.Schedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
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
    override fun onDeleteReminder(scheduleId: String) = Unit
    override fun onUndoDelete() = Unit
    override fun onDeletedMessageShown() = Unit

    companion object {
        val defaultReminders: List<ReminderItem> =
            listOf(
                ReminderItem(
                    id = "daily",
                    nextAt = LocalDateTime(year = 2026, month = 6, day = 20, hour = 10, minute = 0),
                    schedule = Schedule.Daily(LocalTime(hour = 10, minute = 0)),
                ),
                ReminderItem(
                    id = "weekly",
                    nextAt = LocalDateTime(year = 2026, month = 6, day = 22, hour = 14, minute = 30),
                    schedule = Schedule.Weekly(
                        time = LocalTime(hour = 14, minute = 30),
                        day = DayOfWeek.MONDAY,
                    ),
                ),
                ReminderItem(
                    id = "workday-period",
                    nextAt = LocalDateTime(year = 2026, month = 6, day = 22, hour = 9, minute = 20),
                    schedule = Schedule.WorkdayPeriod(
                        from = LocalTime(hour = 9, minute = 0),
                        until = LocalTime(hour = 18, minute = 0),
                        intervalMinutes = 20,
                        days = listOf(
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY,
                        ),
                    ),
                ),
            )
    }
}
