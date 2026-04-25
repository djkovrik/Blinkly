package com.sedsoftware.blinkly.component.onboarding.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.onboarding.OnboardingComponent
import com.sedsoftware.blinkly.component.step1.integration.OnboardingStep1ComponentPreview
import com.sedsoftware.blinkly.component.step2.integration.OnboardingStep2ComponentPreview
import com.sedsoftware.blinkly.component.step3.integration.OnboardingStep3ComponentPreview
import com.sedsoftware.blinkly.component.step4.integration.OnboardingStep4ComponentPreview
import com.sedsoftware.blinkly.component.step5.integration.OnboardingStep5ComponentPreview
import com.sedsoftware.blinkly.utils.PreviewComponentContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

class OnboardingComponentPreview(
    step: Int,
    skipInitialSetup: Boolean = false,
    selectedTimeFrom: LocalTime = LocalTime(0, 0),
    selectedTimeUntil: LocalTime = LocalTime(1, 0),
    selectedInterval: Int = 1,
    selectedDays: List<DayOfWeek> = DayOfWeek.entries.toList(),
    initialReminderDays: List<DayOfWeek> = emptyList(),
    initialReminderTimes: List<LocalTime> = emptyList(),
    initialRemindersVisible: Boolean = false,
) : OnboardingComponent, ComponentContext by PreviewComponentContext {

    override val childStack: Value<ChildStack<*, OnboardingComponent.Child>> =
        MutableValue(
            initialValue = ChildStack(
                configuration = Unit,
                instance = when (step) {
                    1 -> OnboardingComponent.Child.Step1(OnboardingStep1ComponentPreview())
                    2 -> OnboardingComponent.Child.Step2(OnboardingStep2ComponentPreview())
                    3 -> OnboardingComponent.Child.Step3(OnboardingStep3ComponentPreview())
                    4 -> OnboardingComponent.Child.Step4(OnboardingStep4ComponentPreview(true))

                    else -> OnboardingComponent.Child.Step5(
                        OnboardingStep5ComponentPreview(
                            skipInitialSetup = skipInitialSetup,
                            selectedTimeFrom = selectedTimeFrom,
                            selectedTimeUntil = selectedTimeUntil,
                            selectedInterval = selectedInterval,
                            selectedDays = selectedDays,
                            initialReminderDays = initialReminderDays,
                            initialReminderTimes = initialReminderTimes,
                            initialRemindersVisible = initialRemindersVisible,
                        )
                    )
                }
            )
        )

    override fun onBack() = Unit
}
