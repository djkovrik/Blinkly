package com.sedsoftware.blinkly.component.newreminder.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.newreminder.domain.model.ReminderScheduleType
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.Intent
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.Label
import com.sedsoftware.blinkly.component.newreminder.store.AddNewReminderStore.State
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal interface AddNewReminderStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class ScheduleTypeSelected(val type: ReminderScheduleType) : Intent
        data class DailyTimeSelected(val time: LocalTime) : Intent
        data class WeeklyTimeSelected(val time: LocalTime) : Intent
        data class WeeklyDaySelected(val dayOfWeek: DayOfWeek) : Intent
        data class PeriodTimeFromSelected(val time: LocalTime) : Intent
        data class PeriodTimeUntilSelected(val time: LocalTime) : Intent
        data class PeriodIntervalSelected(val interval: Int) : Intent
        data class PeriodDayToggled(val dayOfWeek: DayOfWeek) : Intent
        data object CreateClicked : Intent
        data object ValidationMessageShown : Intent
    }

    data class State(
        val scheduleType: ReminderScheduleType = ReminderScheduleType.DAILY,
        val dailyTime: LocalTime = LocalTime(DEFAULT_DAILY_HOUR, 0),
        val weeklyTime: LocalTime = LocalTime(DEFAULT_DAILY_HOUR, 0),
        val weeklyDay: DayOfWeek = DayOfWeek.MONDAY,
        val periodTimeFrom: LocalTime = LocalTime(DEFAULT_PERIOD_START_HOUR, 0),
        val periodTimeUntil: LocalTime = LocalTime(DEFAULT_PERIOD_END_HOUR, 0),
        val periodInterval: Int = DEFAULT_INTERVAL,
        val periodDays: List<DayOfWeek> = DayOfWeek.entries.toList(),
        val isSaving: Boolean = false,
        val validationError: ValidationError? = null,
    )

    enum class ValidationError {
        EMPTY_DAYS,
        INVALID_PERIOD,
        INVALID_INTERVAL,
    }

    sealed class Label {
        data object ReminderCreated : Label()
        data class ErrorCaught(val exception: Throwable) : Label()
    }

    private companion object {
        const val DEFAULT_DAILY_HOUR = 10
        const val DEFAULT_PERIOD_START_HOUR = 9
        const val DEFAULT_PERIOD_END_HOUR = 18
        const val DEFAULT_INTERVAL = 20
    }
}
