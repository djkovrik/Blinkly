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

class OnboardingComponentPreview(
    step: Int,
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
                    else -> OnboardingComponent.Child.Step5(OnboardingStep5ComponentPreview())
                }
            )
        )

    override fun onBack() = Unit
}
