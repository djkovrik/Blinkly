package com.sedsoftware.blinkly.component.preferences.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStore.Intent
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStore.Label
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStore.State
import com.sedsoftware.blinkly.domain.model.ThemeState

internal interface PreferencesStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class BlinkBreakCountChanged(val value: Int) : Intent
        data class NearFarFocusCountChanged(val value: Int) : Intent
        data class NearFarFocusDurationChanged(val value: Float) : Intent
        data class DiagonalGazesCountChanged(val value: Int) : Intent
        data class DiagonalGazesDurationChanged(val value: Float) : Intent
        data class FigureEightCountChanged(val value: Int) : Intent
        data class ClockRollsEachSideChanged(val value: Int) : Intent
        data class PalmingDurationChanged(val value: Int) : Intent
        data class ThemeStateChanged(val value: ThemeState) : Intent
    }

    data class State(
        val blinkBreakCount: Int = 0,
        val nearFarFocusCount: Int = 0,
        val nearFarFocusDuration: Float = 0f,
        val diagonalGazesCount: Int = 0,
        val diagonalGazesDuration: Float = 0f,
        val figureEightCount: Int = 0,
        val clockRollsEachSide: Int = 0,
        val palmingDuration: Int = 0,
        val themeState: ThemeState = ThemeState.SYSTEM,
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
        data class ThemeStateChanged(val value: ThemeState) : Label()
    }
}
