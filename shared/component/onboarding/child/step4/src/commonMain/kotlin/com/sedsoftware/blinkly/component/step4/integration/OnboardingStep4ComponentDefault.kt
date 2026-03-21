package com.sedsoftware.blinkly.component.step4.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class OnboardingStep4ComponentDefault(
    private val componentContext: ComponentContext,
    private val onboardingOutput: (ComponentOutput) -> Unit,
) : OnboardingStep4Component, ComponentContext by componentContext {

    override fun nextStep() {
        onboardingOutput(ComponentOutput.Onboarding.GoToStep5)
    }

    override fun previousStep() {
        onboardingOutput(ComponentOutput.Onboarding.GoBack)
    }
}
