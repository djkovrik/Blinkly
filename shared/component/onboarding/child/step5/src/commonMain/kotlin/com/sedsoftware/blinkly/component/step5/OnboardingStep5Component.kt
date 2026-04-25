package com.sedsoftware.blinkly.component.step5

import com.arkivanov.decompose.value.Value
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

interface OnboardingStep5Component {

    val model: Value<Model>
    fun onNextClick()
    fun onBackClick()
    fun onInitialSetupChoice(agree: Boolean)
    fun onSelectTimeFrom(time: LocalTime)
    fun onSelectTimeUntil(time: LocalTime)
    fun onSelectInterval(interval: Int)
    fun onToggleDay(weekDay: DayOfWeek)
    fun onCreateReminders()
    fun onClearReminders()

    data class Model(
        val showInitialSetup: Boolean,
        val selectedTimeFrom: LocalTime,
        val selectedTimeUntil: LocalTime,
        val selectedInterval: Int,
        val selectedDays: List<DayOfWeek>,
        val initialReminderDays: List<DayOfWeek>,
        val initialReminderTimes: List<LocalTime>,
        val initialRemindersVisible: Boolean,
    )
}
