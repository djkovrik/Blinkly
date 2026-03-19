package com.sedsoftware.blinkly.domain.fakes

import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.LocalDate

class FakeSettings : BlinklySettings {
    override var blinkBreakCount: Int = FakeData.BLINK_BREAK_COUNT
    override var nearFarFocusCount: Int = FakeData.NEAR_FAR_COUNT
    override var nearFarFocusDuration: Float = FakeData.NEAR_FAR_DURATION
    override var diagonalGazesCount: Int = FakeData.DIAGONAL_COUNT
    override var diagonalGazesDuration: Float = FakeData.DIAGONAL_DURATION
    override var figureEightCount: Int = FakeData.FIGURE_EIGHT_COUNT
    override var clockRollsEachSide: Int = FakeData.CLOCK_COUNT
    override var palmingDuration: Int = FakeData.PALMING_DURATION
    override var themeState: ThemeState = ThemeState.SYSTEM
    override var lightThemeWorkoutIndex: Int = 0
    override var darkThemeWorkoutIndex: Int = 0
    override var lastTreeProgressCheckDate: LocalDate? = null
    override var displayedHighlights: List<Int> = emptyList()
    override var currentHighlightDate: LocalDate? = null
    override var onboardingDisplayed: Boolean = false
}
