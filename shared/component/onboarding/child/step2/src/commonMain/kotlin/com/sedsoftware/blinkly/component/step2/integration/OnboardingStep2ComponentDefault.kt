package com.sedsoftware.blinkly.component.step2.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.step2.OnboardingStep2Component
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class OnboardingStep2ComponentDefault(
    private val componentContext: ComponentContext,
    private val onboardingOutput: (ComponentOutput) -> Unit,
) : OnboardingStep2Component, ComponentContext by componentContext {

    override fun onNextClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoToStep3)
    }

    override fun onBackClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoBack)
    }
}
