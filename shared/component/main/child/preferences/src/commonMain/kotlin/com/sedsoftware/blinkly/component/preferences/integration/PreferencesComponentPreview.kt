package com.sedsoftware.blinkly.component.preferences.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.preferences.PreferencesComponent
import com.sedsoftware.blinkly.component.preferences.PreferencesComponent.Model
import com.sedsoftware.blinkly.domain.model.ThemeState

class PreferencesComponentPreview(
    private val blinkBreakCount: Int = 60,
    private val nearFarFocusCount: Int = 10,
    private val nearFarFocusDuration: Float = 5f,
    private val diagonalGazesCount: Int = 5,
    private val diagonalGazesDuration: Float = 3f,
    private val figureEightCount: Int = 10,
    private val clockRollsEachSide: Int = 5,
    private val palmingDuration: Int = 120,
    private val themeState: ThemeState = ThemeState.SYSTEM,
) : PreferencesComponent {

    override val model: Value<Model> = MutableValue(
        Model(
            blinkBreakCount = blinkBreakCount,
            nearFarFocusCount = nearFarFocusCount,
            nearFarFocusDuration = nearFarFocusDuration,
            diagonalGazesCount = diagonalGazesCount,
            diagonalGazesDuration = diagonalGazesDuration,
            figureEightCount = figureEightCount,
            clockRollsEachSide = clockRollsEachSide,
            palmingDuration = palmingDuration,
            themeState = themeState,
        )
    )

    override fun onBackClick() = Unit
    override fun onBlinkBreakCountChanged(value: Int) = Unit
    override fun onNearFarFocusCountChanged(value: Int) = Unit
    override fun onNearFarFocusDurationChanged(value: Float) = Unit
    override fun onDiagonalGazesCountChanged(value: Int) = Unit
    override fun onDiagonalGazesDurationChanged(value: Float) = Unit
    override fun onFigureEightCountChanged(value: Int) = Unit
    override fun onClockRollsEachSideChanged(value: Int) = Unit
    override fun onPalmingDurationChanged(value: Int) = Unit
    override fun onThemeStateChanged(value: ThemeState) = Unit
}
