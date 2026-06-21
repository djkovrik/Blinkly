package com.sedsoftware.blinkly.component.newreminder

import com.arkivanov.decompose.value.Value
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

interface AddNewReminderComponent {
    val model: Value<Model>

    fun onBackClick()
    fun onScheduleTypeSelect(type: ScheduleType)
    fun onDailyTimeSelect(time: LocalTime)
    fun onWeeklyTimeSelect(time: LocalTime)
    fun onWeeklyDaySelect(dayOfWeek: DayOfWeek)
    fun onPeriodTimeFromSelect(time: LocalTime)
    fun onPeriodTimeUntilSelect(time: LocalTime)
    fun onPeriodIntervalSelect(interval: Int)
    fun onPeriodDayToggle(dayOfWeek: DayOfWeek)
    fun onCreateClick()
    fun onValidationMessageShown()

    data class Model(
        val scheduleType: ScheduleType,
        val dailyTime: LocalTime,
        val weeklyTime: LocalTime,
        val weeklyDay: DayOfWeek,
        val periodTimeFrom: LocalTime,
        val periodTimeUntil: LocalTime,
        val periodInterval: Int,
        val periodDays: List<DayOfWeek>,
        val isSaving: Boolean,
        val validationError: ValidationError?,
    )

    enum class ScheduleType {
        DAILY,
        WEEKLY_SINGLE,
        WEEKLY_DAY_PERIOD,
    }

    enum class ValidationError {
        EMPTY_DAYS,
        INVALID_PERIOD,
        INVALID_INTERVAL,
    }
}
