package com.sedsoftware.blinkly.domain.model

sealed class ComponentOutput {

    sealed class Onboarding : ComponentOutput() {
        data object GoToStep2 : Onboarding()
        data object GoToStep3 : Onboarding()
        data object GoToStep4 : Onboarding()
        data object GoToStep5 : Onboarding()
        data object GoBack : Onboarding()
    }

    sealed class Common : ComponentOutput() {
        data object OpenHomeScreen : ComponentOutput()
        data class ErrorCaught(val throwable: Throwable) : Common()
    }
}
