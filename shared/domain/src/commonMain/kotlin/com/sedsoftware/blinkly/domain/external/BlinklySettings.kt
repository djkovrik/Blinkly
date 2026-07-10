package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

interface BlinklySettings {
    var blinkBreakCount: Int
    var nearFarFocusCount: Int
    var nearFarFocusDuration: Float
    var diagonalGazesCount: Int
    var diagonalGazesDuration: Float
    var figureEightCount: Int
    var clockRollsEachSide: Int
    var palmingDuration: Int
    var themeState: ThemeState
    var lightThemeWorkoutIndex: Int
    var darkThemeWorkoutIndex: Int
    var lastTreeProgressCheckDate: LocalDate?
    var displayedHighlights: List<Int>
    var currentHighlightDate: LocalDate?
    var onboardingDisplayed: Boolean
    var lastLocalDatabaseChangeAt: Instant?
    var lastLocalSettingsChangeAt: Instant?
    var lastSyncedAt: Instant?
    var lastRemoteUpdatedAt: Instant?
}
