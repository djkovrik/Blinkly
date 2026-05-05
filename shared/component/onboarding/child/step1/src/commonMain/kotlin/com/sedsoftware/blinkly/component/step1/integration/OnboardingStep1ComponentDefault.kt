package com.sedsoftware.blinkly.component.step1.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.step1.OnboardingStep1Component
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class OnboardingStep1ComponentDefault(
    private val componentContext: ComponentContext,
    private val onboardingOutput: (ComponentOutput) -> Unit,
) : OnboardingStep1Component, ComponentContext by componentContext {

    override fun onNextClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoToStep2)
    }
}
