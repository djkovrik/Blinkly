package com.sedsoftware.blinkly.component.preferences.integration

import com.sedsoftware.blinkly.component.preferences.PreferencesComponent.Model
import com.sedsoftware.blinkly.component.preferences.store.PreferencesStore.State

internal val stateToModel: (State) -> Model = {
    Model(
        blinkBreakCount = it.blinkBreakCount,
        nearFarFocusCount = it.nearFarFocusCount,
        nearFarFocusDuration = it.nearFarFocusDuration,
        diagonalGazesCount = it.diagonalGazesCount,
        diagonalGazesDuration = it.diagonalGazesDuration,
        figureEightCount = it.figureEightCount,
        clockRollsEachSide = it.clockRollsEachSide,
        palmingDuration = it.palmingDuration,
        themeState = it.themeState,
    )
}
