package com.sedsoftware.blinkly.component.step5.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component.Model
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class OnboardingStep5ComponentPreview(
    val showInitialSetup: Boolean,
    val selectedTimeFrom: LocalTime,
    val selectedTimeUntil: LocalTime,
    val selectedInterval: Int,
    val selectedDays: List<DayOfWeek>,
    val createdRemindersCount: Int,
) : OnboardingStep5Component {

    override val model: Value<Model> =
        MutableValue(
            Model(
                showInitialSetup = showInitialSetup,
                selectedTimeFrom = selectedTimeFrom,
                selectedTimeUntil = selectedTimeUntil,
                selectedInterval = selectedInterval,
                selectedDays = selectedDays,
                createdRemindersCount = createdRemindersCount,
            )
        )

    override fun onNextClick() = Unit
    override fun onBackClick() = Unit
    override fun onInitialSetupChoice(agree: Boolean) = Unit
    override fun onSelectTimeFrom(time: LocalTime) = Unit
    override fun onSelectTimeUntil(time: LocalTime) = Unit
    override fun onSelectInterval(interval: Int) = Unit
    override fun onToggleDay(weekDay: DayOfWeek) = Unit
    override fun onCreateReminders() = Unit
    override fun onClearReminders() = Unit
}
