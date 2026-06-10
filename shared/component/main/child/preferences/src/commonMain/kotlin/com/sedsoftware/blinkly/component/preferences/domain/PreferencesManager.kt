package com.sedsoftware.blinkly.component.preferences.domain

import com.sedsoftware.blinkly.component.preferences.domain.model.PreferencesData
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ThemeState

internal class PreferencesManager(
    private val settings: BlinklySettings,
) {

    fun load(): Result<PreferencesData> =
        runCatching {
            PreferencesData(
                blinkBreakCount = settings.blinkBreakCount,
                nearFarFocusCount = settings.nearFarFocusCount,
                nearFarFocusDuration = settings.nearFarFocusDuration,
                diagonalGazesCount = settings.diagonalGazesCount,
                diagonalGazesDuration = settings.diagonalGazesDuration,
                figureEightCount = settings.figureEightCount,
                clockRollsEachSide = settings.clockRollsEachSide,
                palmingDuration = settings.palmingDuration,
                themeState = settings.themeState,
            )
        }

    fun saveBlinkBreakCount(value: Int): Result<Unit> =
        runCatching {
            settings.blinkBreakCount = value
        }

    fun saveNearFarFocusCount(value: Int): Result<Unit> =
        runCatching {
            settings.nearFarFocusCount = value
        }

    fun saveNearFarFocusDuration(value: Float): Result<Unit> =
        runCatching {
            settings.nearFarFocusDuration = value
        }

    fun saveDiagonalGazesCount(value: Int): Result<Unit> =
        runCatching {
            settings.diagonalGazesCount = value
        }

    fun saveDiagonalGazesDuration(value: Float): Result<Unit> =
        runCatching {
            settings.diagonalGazesDuration = value
        }

    fun saveFigureEightCount(value: Int): Result<Unit> =
        runCatching {
            settings.figureEightCount = value
        }

    fun saveClockRollsEachSide(value: Int): Result<Unit> =
        runCatching {
            settings.clockRollsEachSide = value
        }

    fun savePalmingDuration(value: Int): Result<Unit> =
        runCatching {
            settings.palmingDuration = value
        }

    fun saveThemeState(value: ThemeState): Result<Unit> =
        runCatching {
            settings.themeState = value
        }
}
