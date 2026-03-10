package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.fakes.FakeData
import com.sedsoftware.blinkly.domain.internal.CalendarWatcherImpl
import com.sedsoftware.blinkly.domain.model.Workout
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CalendarWatcherTest : BaseDomainTest() {

    private val calendarFlow: MutableStateFlow<List<Workout>> = MutableStateFlow(emptyList())

    private val database: BlinklyDatabase = mock {
        everySuspend { currentCalendar() } returns calendarFlow
        everySuspend { saveExercise(any()) } returns Unit
        everySuspend { currentAchievements() } returns MutableStateFlow(emptyList())
        everySuspend { unlockAchievement(any()) } returns Unit
    }

    private val watcher: CalendarWatcher = CalendarWatcherImpl(database, testDispatchers)

    @Test
    fun `calendar flow emits workout list`() = runTest(testScheduler) {
        // given
        val workout = FakeData.getSingleExerciseWorkout(now)
        val expectedCalendar: List<Workout> = listOf(workout)

        // when
        val collectJob = launch { watcher.calendar.collect {} }

        calendarFlow.emit(expectedCalendar)
        testScheduler.advanceUntilIdle()

        // then
        val actualCalendar = watcher.calendar.first()
        assertThat(expectedCalendar).isEqualTo(actualCalendar)

        collectJob.cancel()
    }

    @Test
    fun `calendar flow emits empty list when no workouts`() = runTest(testScheduler) {
        // when
        val collectJob = launch { watcher.calendar.collect {} }

        calendarFlow.emit(emptyList())
        testScheduler.advanceUntilIdle()

        // then
        val actualCalendar = watcher.calendar.first()
        assertThat(actualCalendar).isEqualTo(emptyList())

        collectJob.cancel()
    }
}
