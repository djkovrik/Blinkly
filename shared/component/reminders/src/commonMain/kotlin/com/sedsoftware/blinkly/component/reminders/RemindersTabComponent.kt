package com.sedsoftware.blinkly.component.reminders

import com.arkivanov.decompose.value.Value
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

interface RemindersTabComponent {
    val model: Value<Model>

    fun onAddNewClick()
    fun onDeleteReminder(scheduleId: String)
    fun onUndoDelete()
    fun onDeletedMessageShown()

    data class Model(
        val reminders: List<ReminderItem>,
        val deletedReminder: ReminderItem?,
    )

    data class ReminderItem(
        val id: String,
        val nextAt: LocalDateTime,
        val schedule: Schedule,
    )

    sealed interface Schedule {
        data class Daily(val time: LocalTime) : Schedule
        data class Weekly(val time: LocalTime, val day: DayOfWeek) : Schedule
        data class WorkdayPeriod(
            val from: LocalTime,
            val until: LocalTime,
            val intervalMinutes: Int,
            val days: List<DayOfWeek>,
        ) : Schedule
    }
}
