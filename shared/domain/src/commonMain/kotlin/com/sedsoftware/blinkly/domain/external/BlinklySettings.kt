package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.LocalDate

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
}
