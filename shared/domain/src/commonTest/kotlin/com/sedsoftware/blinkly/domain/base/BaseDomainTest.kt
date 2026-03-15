package com.sedsoftware.blinkly.domain.base

import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.fakes.FakeSettings
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
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
        everySuspend { achievementUnlocked(any()) } returns Unit
    }

    protected val settings: FakeSettings = FakeSettings()

    protected val timeUtils: BlinklyTimeUtils = mock {
        every { now() } returns now
        every { timeZone() } returns TimeZone.UTC
    }
}
