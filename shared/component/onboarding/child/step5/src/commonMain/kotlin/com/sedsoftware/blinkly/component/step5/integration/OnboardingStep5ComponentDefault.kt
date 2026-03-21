package com.sedsoftware.blinkly.component.step5.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.step5.OnboardingStep5Component
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class OnboardingStep5ComponentDefault(
    private val componentContext: ComponentContext,
    private val onboardingOutput: (ComponentOutput) -> Unit,
) : OnboardingStep5Component, ComponentContext by componentContext {

    override fun nextStep() {
        onboardingOutput(ComponentOutput.Common.OpenHomeScreen)
    }

    override fun previousStep() {
        onboardingOutput(ComponentOutput.Onboarding.GoBack)
    }
}
