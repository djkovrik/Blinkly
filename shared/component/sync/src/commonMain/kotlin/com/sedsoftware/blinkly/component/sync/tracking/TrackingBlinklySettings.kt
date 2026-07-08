package com.sedsoftware.blinkly.component.sync.tracking

import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

internal class TrackingBlinklySettings(
    private val delegate: BlinklySettings,
    private val timeUtils: BlinklyTimeUtils,
) : BlinklySettings {

    override var blinkBreakCount: Int
        get() = delegate.blinkBreakCount
        set(value) = setTracked { delegate.blinkBreakCount = value }

    override var nearFarFocusCount: Int
        get() = delegate.nearFarFocusCount
        set(value) = setTracked { delegate.nearFarFocusCount = value }

    override var nearFarFocusDuration: Float
        get() = delegate.nearFarFocusDuration
        set(value) = setTracked { delegate.nearFarFocusDuration = value }

    override var diagonalGazesCount: Int
        get() = delegate.diagonalGazesCount
        set(value) = setTracked { delegate.diagonalGazesCount = value }

    override var diagonalGazesDuration: Float
        get() = delegate.diagonalGazesDuration
        set(value) = setTracked { delegate.diagonalGazesDuration = value }

    override var figureEightCount: Int
        get() = delegate.figureEightCount
        set(value) = setTracked { delegate.figureEightCount = value }

    override var clockRollsEachSide: Int
        get() = delegate.clockRollsEachSide
        set(value) = setTracked { delegate.clockRollsEachSide = value }

    override var palmingDuration: Int
        get() = delegate.palmingDuration
        set(value) = setTracked { delegate.palmingDuration = value }

    override var themeState: ThemeState
        get() = delegate.themeState
        set(value) = setTracked { delegate.themeState = value }

    override var lightThemeWorkoutIndex: Int
        get() = delegate.lightThemeWorkoutIndex
        set(value) = setTracked { delegate.lightThemeWorkoutIndex = value }

    override var darkThemeWorkoutIndex: Int
        get() = delegate.darkThemeWorkoutIndex
        set(value) = setTracked { delegate.darkThemeWorkoutIndex = value }

    override var lastTreeProgressCheckDate: LocalDate?
        get() = delegate.lastTreeProgressCheckDate
        set(value) = setTracked { delegate.lastTreeProgressCheckDate = value }

    override var displayedHighlights: List<Int>
        get() = delegate.displayedHighlights
        set(value) = setTracked { delegate.displayedHighlights = value }

    override var currentHighlightDate: LocalDate?
        get() = delegate.currentHighlightDate
        set(value) = setTracked { delegate.currentHighlightDate = value }

    override var onboardingDisplayed: Boolean
        get() = delegate.onboardingDisplayed
        set(value) = setTracked { delegate.onboardingDisplayed = value }

    override var lastLocalChangeAt: Instant?
        get() = delegate.lastLocalChangeAt
        set(value) {
            delegate.lastLocalChangeAt = value
        }

    override var lastSyncedAt: Instant?
        get() = delegate.lastSyncedAt
        set(value) {
            delegate.lastSyncedAt = value
        }

    override var lastRemoteUpdatedAt: Instant?
        get() = delegate.lastRemoteUpdatedAt
        set(value) {
            delegate.lastRemoteUpdatedAt = value
        }

    private inline fun setTracked(block: () -> Unit) {
        block()
        delegate.lastLocalChangeAt = timeUtils.now()
    }
}
