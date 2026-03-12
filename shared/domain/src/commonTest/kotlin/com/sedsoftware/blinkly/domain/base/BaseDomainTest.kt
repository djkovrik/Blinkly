package com.sedsoftware.blinkly.domain.base

import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.model.ThemeState
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.time.Clock
import kotlin.time.Instant

abstract class BaseDomainTest {

    protected val testScheduler: TestCoroutineScheduler = TestCoroutineScheduler()

    protected val testDispatchers: BlinklyDispatchers =
        object : BlinklyDispatchers {
            override val main: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
            override val io: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
        }

    protected val now: Instant
        get() = Clock.System.now()

    protected val yesterday: Instant
        get() = now.minus(1, DateTimeUnit.DAY, TimeZone.UTC)

    protected val notifier: BlinklyNotifier = mock {
        every { achievementUnlocked(any()) } returns Unit
    }

    protected val settings: BlinklySettings = mock {
        every { blinkBreakCount } returns FakeData.BLINK_BREAK_COUNT
        every { nearFarFocusCount } returns FakeData.NEAR_FAR_COUNT
        every { nearFarFocusDuration } returns FakeData.NEAR_FAR_DURATION
        every { diagonalGazesCount } returns FakeData.DIAGONAL_COUNT
        every { diagonalGazesDuration } returns FakeData.DIAGONAL_DURATION
        every { figureEightCount } returns FakeData.FIGURE_EIGHT_COUNT
        every { clockRollsEachSide } returns FakeData.CLOCK_COUNT
        every { palmingDuration } returns FakeData.PALMING_DURATION
        every { themeState } returns ThemeState.SYSTEM
        every { lightThemeWorkoutIndex } returns 0
        every { darkThemeWorkoutIndex } returns 0
    }

    protected val timeUtils: BlinklyTimeUtils = mock {
        every { now() } returns now
    }
}
