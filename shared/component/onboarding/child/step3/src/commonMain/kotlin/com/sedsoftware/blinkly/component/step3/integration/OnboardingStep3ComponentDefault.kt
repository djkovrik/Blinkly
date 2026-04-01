package com.sedsoftware.blinkly.component.step3.integration

import com.arkivanov.decompose.ComponentContext
import com.sedsoftware.blinkly.component.step3.OnboardingStep3Component
import com.sedsoftware.blinkly.domain.model.ComponentOutput

class OnboardingStep3ComponentDefault(
    private val componentContext: ComponentContext,
    private val onboardingOutput: (ComponentOutput) -> Unit,
) : OnboardingStep3Component, ComponentContext by componentContext {

    override fun onNextClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoToStep4)
    }

    override fun onBackClick() {
        onboardingOutput(ComponentOutput.Onboarding.GoBack)
    }
}
