package com.sedsoftware.blinkly.domain.base

import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.ThemeState
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.time.Instant

abstract class BaseDomainTest {

    protected val testScheduler: TestCoroutineScheduler = TestCoroutineScheduler()

    protected val testDispatchers: BlinklyDispatchers =
        object : BlinklyDispatchers {
            override val main: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
            override val io: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
        }

    protected val now: Instant = Instant.DISTANT_PAST

    protected val notifier: BlinklyNotifier = mock {
        every { achievementUnlocked(any()) } returns Unit
    }

    protected val settings: BlinklySettings = mock {
        every { blinkBreakCount } returns 60
        every { nearFarFocusCount } returns 10
        every { nearFarFocusDuration } returns 10
        every { diagonalGazesCount } returns 5
        every { figureEightCount } returns 10
        every { clockRollsEachSide } returns 5
        every { palmingDuration } returns 120
        every { themeState } returns ThemeState.SYSTEM
        every { lightThemeWorkoutDone } returns false
        every { darkThemeWorkoutDone } returns false
    }

    protected val timeUtils: BlinklyTimeUtils = mock {
        every { now() } returns now
    }
}
