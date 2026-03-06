package com.sedsoftware.blinkly.domain.external

interface BlinklySettings {         // Default:
    var blinkBreakCount: Int        // 60
    var nearFarFocusCount: Int      // 10
    var nearFarFocusDuration: Int   // 10 sec
    var diagonalGazesCount: Int     // 5
    var figureEightCount: Int       // 10
    var clockRollsEachSide: Int     // 5
    var palmingDuration: Int        // 120 sec
}
