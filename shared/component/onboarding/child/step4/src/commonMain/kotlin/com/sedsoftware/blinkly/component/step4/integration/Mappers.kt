package com.sedsoftware.blinkly.component.step4.integration

import com.sedsoftware.blinkly.component.step4.OnboardingStep4Component.Model
import com.sedsoftware.blinkly.component.step4.store.DisclaimerStore.State

internal val stateToModel: (State) -> Model = {
    Model(
        checkboxSelected = it.checkboxSelected,
    )
}
