package com.sedsoftware.blinkly.component.progress.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.progress.domain.model.ProgressCalendarDay
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStore.Intent
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStore.Label
import com.sedsoftware.blinkly.component.progress.store.ProgressTabStore.State
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Tree

internal interface ProgressTabStore : Store<Intent, State, Label> {

    sealed interface Intent

    data class State(
        val calendarWeeks: List<List<ProgressCalendarDay?>> = emptyList(),
        val tree: Tree? = null,
        val recentAchievements: List<Achievement?> = listOf(null, null, null),
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }
}
