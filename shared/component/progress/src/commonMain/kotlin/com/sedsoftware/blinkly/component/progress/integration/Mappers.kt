package com.sedsoftware.blinkly.component.progress.integration

import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.CalendarDay
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.CalendarDayState
import com.sedsoftware.blinkly.component.progress.ProgressTabComponent.Model
import com.sedsoftware.blinkly.component.progress.domain.model.ProgressCalendarDayState
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStore.State

internal val stateToModel: (State) -> Model = { state ->
    Model(
        calendarWeeks = state.calendarWeeks.map { week ->
            week.map { day ->
                day?.let {
                    CalendarDay(
                        date = it.date,
                        state = it.state.toComponentState(),
                    )
                }
            }
        },
        tree = state.tree,
        recentAchievements = state.recentAchievements,
    )
}

private fun ProgressCalendarDayState.toComponentState(): CalendarDayState =
    when (this) {
        ProgressCalendarDayState.EMPTY -> CalendarDayState.EMPTY
        ProgressCalendarDayState.WORKOUT -> CalendarDayState.WORKOUT
        ProgressCalendarDayState.PERFECT -> CalendarDayState.PERFECT
    }
