package com.sedsoftware.blinkly.component.preferences

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.domain.model.ThemeState

interface PreferencesComponent {

    val model: Value<Model>

    fun onBackClick()
    fun onBlinkBreakCountChanged(value: Int)
    fun onNearFarFocusCountChanged(value: Int)
    fun onNearFarFocusDurationChanged(value: Float)
    fun onDiagonalGazesCountChanged(value: Int)
    fun onDiagonalGazesDurationChanged(value: Float)
    fun onFigureEightCountChanged(value: Int)
    fun onClockRollsEachSideChanged(value: Int)
    fun onPalmingDurationChanged(value: Int)
    fun onThemeStateChanged(value: ThemeState)

    data class Model(
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
}
