package com.sedsoftware.blinkly.component.step2.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.step2.OnboardingStep2Component
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class OnboardingStep2ComponentDefault(
    private val componentContext: ComponentContext,
    private val onboardingOutput: (ComponentOutput) -> Unit,
) : OnboardingStep2Component, ComponentContext by componentContext {

    override fun nextStep() {
        onboardingOutput(ComponentOutput.Onboarding.GoToStep3)
    }

    override fun previousStep() {
        onboardingOutput(ComponentOutput.Onboarding.GoBack)
    }
}
