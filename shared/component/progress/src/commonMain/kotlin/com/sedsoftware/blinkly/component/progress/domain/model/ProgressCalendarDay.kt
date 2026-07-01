package com.sedsoftware.blinkly.component.progress.domain.model

import kotlinx.datetime.LocalDate

internal data class ProgressCalendarDay(
    val date: LocalDate,
    val state: ProgressCalendarDayState,
)

internal enum class ProgressCalendarDayState {
    EMPTY,
    WORKOUT,
    PERFECT,
}
