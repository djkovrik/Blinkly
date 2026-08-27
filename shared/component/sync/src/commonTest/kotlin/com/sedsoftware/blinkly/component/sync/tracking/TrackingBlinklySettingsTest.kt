package com.sedsoftware.blinkly.component.sync.tracking

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ThemeState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.time.Instant

class TrackingBlinklySettingsTest {

    @Test
    fun `analytics preference remains device local and does not advance sync timestamp`() {
        val delegate = FakeSettings()
        val now = Instant.fromEpochMilliseconds(42)
        val settings = TrackingBlinklySettings(
            delegate = delegate,
            timeUtils = object : BlinklyTimeUtils {
                override fun now(): Instant = now
                override fun timeZone(): TimeZone = TimeZone.UTC
            },
        )

        settings.analyticsEnabled = false

        assertThat(delegate.analyticsEnabled).isEqualTo(false)
        assertThat(delegate.lastLocalSettingsChangeAt).isEqualTo(null)

        settings.blinkBreakCount = 80

        assertThat(delegate.lastLocalSettingsChangeAt).isEqualTo(now)
    }

    private class FakeSettings : BlinklySettings {
        override var analyticsEnabled: Boolean = true
        override var blinkBreakCount: Int = 60
        override var nearFarFocusCount: Int = 10
        override var nearFarFocusDuration: Float = 5f
        override var diagonalGazesCount: Int = 5
        override var diagonalGazesDuration: Float = 3f
        override var figureEightCount: Int = 10
        override var clockRollsEachSide: Int = 5
        override var palmingDuration: Int = 120
        override var themeState: ThemeState = ThemeState.SYSTEM
        override var lightThemeWorkoutIndex: Int = 0
        override var darkThemeWorkoutIndex: Int = 0
        override var lastTreeProgressCheckDate: LocalDate? = null
        override var displayedHighlights: List<Int> = emptyList()
        override var currentHighlightDate: LocalDate? = null
        override var onboardingDisplayed: Boolean = false
        override var lastLocalDatabaseChangeAt: Instant? = null
        override var lastLocalSettingsChangeAt: Instant? = null
        override var lastSyncedAt: Instant? = null
        override var lastRemoteUpdatedAt: Instant? = null
    }
}
