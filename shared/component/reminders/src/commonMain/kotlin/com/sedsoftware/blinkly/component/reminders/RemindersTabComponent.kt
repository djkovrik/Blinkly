package com.sedsoftware.blinkly.component.reminders

import com.arkivanov.decompose.value.Value
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface RemindersTabComponent {
    val model: Value<Model>

    fun onAddNewClick()
    fun onDeleteReminder(uuid: String)
    fun onUndoDelete()
    fun onDeletedMessageShown()

    data class Model(
        val reminders: List<ReminderItem>,
        val deletedReminder: ReminderItem?,
    )

    data class ReminderItem(
        val uuid: String,
        val title: String,
        val description: String,
        val time: LocalTime,
        val nextDate: LocalDate,
        val interval: Interval,
        val days: List<DayOfWeek>,
    )

    enum class Interval {
        DAILY,
        WEEKLY,
    }
}
