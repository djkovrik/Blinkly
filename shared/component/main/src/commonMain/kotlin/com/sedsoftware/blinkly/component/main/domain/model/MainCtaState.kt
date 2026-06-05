package com.sedsoftware.blinkly.component.main.domain.model

sealed class MainCtaState {
    data object MorningWarmUp : MainCtaState()
    data object WorkBreakDue : MainCtaState()
    data object AfternoonWarmUp : MainCtaState()
    data object EveningRelax : MainCtaState()
    data object DayClosing : MainCtaState()
    data object PerfectDay : MainCtaState()
    data object Idle : MainCtaState()
}
