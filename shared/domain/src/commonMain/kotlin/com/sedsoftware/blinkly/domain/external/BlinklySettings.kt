package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.LocalDate

interface BlinklySettings {                     // Default:
    var blinkBreakCount: Int                    // 60
    var nearFarFocusCount: Int                  // 10
    var nearFarFocusDuration: Float             // 5 sec
    var diagonalGazesCount: Int                 // 5
    var diagonalGazesDuration: Float            // 3 sec
    var figureEightCount: Int                   // 10
    var clockRollsEachSide: Int                 // 5
    var palmingDuration: Int                    // 120 sec

    var themeState: ThemeState                  // SYSTEM
    var lightThemeWorkoutDone: Boolean          // false
    var darkThemeWorkoutDone: Boolean           // false
    var lastTreeProgressCheckDate: LocalDate?   // null
}
