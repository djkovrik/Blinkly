package com.sedsoftware.blinkly.component.preferences.domain.model

import com.sedsoftware.blinkly.domain.model.ThemeState

internal data class PreferencesData(
    val blinkBreakCount: Int,
    val nearFarFocusCount: Int,
    val nearFarFocusDuration: Float,
    val diagonalGazesCount: Int,
    val diagonalGazesDuration: Float,
    val figureEightCount: Int,
    val clockRollsEachSide: Int,
    val palmingDuration: Int,
    val themeState: ThemeState,
)
