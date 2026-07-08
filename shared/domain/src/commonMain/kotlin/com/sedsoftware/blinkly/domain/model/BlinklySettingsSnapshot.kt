package com.sedsoftware.blinkly.domain.model

import com.sedsoftware.blinkly.domain.external.BlinklySettings
import kotlinx.datetime.LocalDate

data class BlinklySettingsSnapshot(
    val blinkBreakCount: Int,
    val nearFarFocusCount: Int,
    val nearFarFocusDuration: Float,
    val diagonalGazesCount: Int,
    val diagonalGazesDuration: Float,
    val figureEightCount: Int,
    val clockRollsEachSide: Int,
    val palmingDuration: Int,
    val themeState: ThemeState,
    val lightThemeWorkoutIndex: Int,
    val darkThemeWorkoutIndex: Int,
    val lastTreeProgressCheckDate: LocalDate?,
    val displayedHighlights: List<Int>,
    val currentHighlightDate: LocalDate?,
    val onboardingDisplayed: Boolean,
)

fun BlinklySettings.toSnapshot(): BlinklySettingsSnapshot =
    BlinklySettingsSnapshot(
        blinkBreakCount = blinkBreakCount,
        nearFarFocusCount = nearFarFocusCount,
        nearFarFocusDuration = nearFarFocusDuration,
        diagonalGazesCount = diagonalGazesCount,
        diagonalGazesDuration = diagonalGazesDuration,
        figureEightCount = figureEightCount,
        clockRollsEachSide = clockRollsEachSide,
        palmingDuration = palmingDuration,
        themeState = themeState,
        lightThemeWorkoutIndex = lightThemeWorkoutIndex,
        darkThemeWorkoutIndex = darkThemeWorkoutIndex,
        lastTreeProgressCheckDate = lastTreeProgressCheckDate,
        displayedHighlights = displayedHighlights,
        currentHighlightDate = currentHighlightDate,
        onboardingDisplayed = onboardingDisplayed,
    )

fun BlinklySettings.applySnapshot(snapshot: BlinklySettingsSnapshot) {
    blinkBreakCount = snapshot.blinkBreakCount
    nearFarFocusCount = snapshot.nearFarFocusCount
    nearFarFocusDuration = snapshot.nearFarFocusDuration
    diagonalGazesCount = snapshot.diagonalGazesCount
    diagonalGazesDuration = snapshot.diagonalGazesDuration
    figureEightCount = snapshot.figureEightCount
    clockRollsEachSide = snapshot.clockRollsEachSide
    palmingDuration = snapshot.palmingDuration
    themeState = snapshot.themeState
    lightThemeWorkoutIndex = snapshot.lightThemeWorkoutIndex
    darkThemeWorkoutIndex = snapshot.darkThemeWorkoutIndex
    lastTreeProgressCheckDate = snapshot.lastTreeProgressCheckDate
    displayedHighlights = snapshot.displayedHighlights
    currentHighlightDate = snapshot.currentHighlightDate
    onboardingDisplayed = snapshot.onboardingDisplayed
}
