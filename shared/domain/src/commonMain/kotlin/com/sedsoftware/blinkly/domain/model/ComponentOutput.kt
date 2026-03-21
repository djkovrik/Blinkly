package com.sedsoftware.blinkly.domain.model

sealed class ComponentOutput {

    sealed class Onboarding : ComponentOutput() {
        data object GoToStep2 : Onboarding()
        data object GoToStep3 : Onboarding()
        data object GoToStep4 : Onboarding()
        data object GoToStep5 : Onboarding()
        data object GoBack : Onboarding()
        data object GoToHomeScreen : Onboarding()
    }

    sealed class Main : ComponentOutput() {
        data object OpenPreferences : Main()
    }

    sealed class Trainings : ComponentOutput() {
        data class OpenExerciseBlock(val block: ExerciseBlock) : Trainings()
    }

    sealed class Progress : ComponentOutput() {
        data object OpenAchievements : Progress()
        data object OpenGarden : Progress()
    }

    sealed class Reminders : ComponentOutput() {
        data object OpenAddNew : Reminders()
    }

    sealed class Common : ComponentOutput() {
        data object BackPressed : Common()
        data class ErrorCaught(val throwable: Throwable) : Common()
    }
}
