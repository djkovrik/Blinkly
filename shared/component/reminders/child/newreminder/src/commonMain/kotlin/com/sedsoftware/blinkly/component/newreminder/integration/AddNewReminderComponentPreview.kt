package com.sedsoftware.blinkly.component.newreminder.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.Model
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.ScheduleType
import com.sedsoftware.blinkly.component.newreminder.AddNewReminderComponent.ValidationError
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class AddNewReminderComponentPreview(
    scheduleType: ScheduleType = ScheduleType.WEEKLY_DAY_PERIOD,
    dailyTime: LocalTime = LocalTime(hour = 10, minute = 0),
    weeklyTime: LocalTime = LocalTime(hour = 12, minute = 30),
    weeklyDay: DayOfWeek = DayOfWeek.MONDAY,
    periodTimeFrom: LocalTime = LocalTime(hour = 9, minute = 0),
    periodTimeUntil: LocalTime = LocalTime(hour = 18, minute = 0),
    periodInterval: Int = 20,
    periodDays: List<DayOfWeek> = DayOfWeek.entries.toList(),
    isSaving: Boolean = false,
    validationError: ValidationError? = null,
) : AddNewReminderComponent {

    override val model: Value<Model> =
        MutableValue(
            Model(
                scheduleType = scheduleType,
                dailyTime = dailyTime,
                weeklyTime = weeklyTime,
                weeklyDay = weeklyDay,
                periodTimeFrom = periodTimeFrom,
                periodTimeUntil = periodTimeUntil,
                periodInterval = periodInterval,
                periodDays = periodDays,
                isSaving = isSaving,
                validationError = validationError,
            )
        )

    override fun onBackClick() = Unit
    override fun onScheduleTypeSelect(type: ScheduleType) = Unit
    override fun onDailyTimeSelect(time: LocalTime) = Unit
    override fun onWeeklyTimeSelect(time: LocalTime) = Unit
    override fun onWeeklyDaySelect(dayOfWeek: DayOfWeek) = Unit
    override fun onPeriodTimeFromSelect(time: LocalTime) = Unit
    override fun onPeriodTimeUntilSelect(time: LocalTime) = Unit
    override fun onPeriodIntervalSelect(interval: Int) = Unit
    override fun onPeriodDayToggle(dayOfWeek: DayOfWeek) = Unit
    override fun onCreateClick() = Unit
    override fun onValidationMessageShown() = Unit
}
