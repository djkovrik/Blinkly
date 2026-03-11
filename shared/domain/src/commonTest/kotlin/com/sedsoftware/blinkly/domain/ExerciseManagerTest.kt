package com.sedsoftware.blinkly.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.internal.ExerciseManagerImpl
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseEvent
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.math.sign
import kotlin.test.Test

class ExerciseManagerTest : BaseDomainTest() {

    private val database: BlinklyDatabase = mock {
        everySuspend { saveExercise(any()) } returns Unit
    }

    private val manager: ExerciseManager = ExerciseManagerImpl(database, settings, timeUtils, testDispatchers)

    @Test
    fun `when block A started then first exercise emits events`() = runTest(testScheduler) {
        // given
        manager.startBlock(ExerciseBlock.A)

        val events = mutableListOf<ExerciseEvent>()

        val job = launch {
            manager.events.toList(events)
        }

        // when
        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(events.any { it is ExerciseEvent.Movement }).isTrue()
        job.cancel()
    }

    @Test
    fun `when block B started then first exercise emits events`() = runTest(testScheduler) {
        // given
        manager.startBlock(ExerciseBlock.B)

        val events = mutableListOf<ExerciseEvent>()

        val job = launch {
            manager.events.toList(events)
        }

        // when
        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(events.any { it is ExerciseEvent.Movement }).isTrue()
        job.cancel()
    }

    @Test
    fun `when block C started then exercise emits events`() = runTest(testScheduler) {
        // given
        manager.startBlock(ExerciseBlock.C)

        val events = mutableListOf<ExerciseEvent>()

        val job = launch {
            manager.events.toList(events)
        }

        // when
        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(events.any { it is ExerciseEvent.Tick }).isTrue()
        job.cancel()
    }

    @Test
    fun `when exercise completed then saved to database`() = runTest(testScheduler) {
        // given
        manager.startBlock(ExerciseBlock.A)

        // when
        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        // then
        verifySuspend { database.saveExercise(any()) }
    }

    @Test
    fun `when pause called then events stop`() = runTest(testScheduler) {
        // given
        manager.startBlock(ExerciseBlock.A)

        val events = mutableListOf<ExerciseEvent>()

        val job = launch {
            manager.events.toList(events)
        }

        manager.startNextExercise()
        manager.pause()
        testScheduler.advanceTimeBy(100)

        // when
        val countBefore = events.size
        testScheduler.advanceTimeBy(1000)

        // then
        assertThat(countBefore.sign).isEqualTo(events.size)

        manager.resume()
        testScheduler.advanceUntilIdle()

        job.cancel()
    }

    @Test
    fun `when resume called then events continue`() = runTest(testScheduler) {
        // given
        manager.startBlock(ExerciseBlock.A)

        val events = mutableListOf<ExerciseEvent>()

        val job = launch {
            manager.events.toList(events)
        }

        manager.startNextExercise()
        manager.pause()
        testScheduler.advanceTimeBy(1000)

        val beforeResume = events.size

        // when
        manager.resume()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(events.size).isGreaterThan(beforeResume)
        job.cancel()
    }

    @Test
    fun `when exercise running then progress events emitted`() = runTest(testScheduler) {
        // given
        manager.startBlock(ExerciseBlock.A)
        val progressEvents = mutableListOf<ExerciseEvent.Progress>()

        val job = launch {
            manager.events.collect {

                if (it is ExerciseEvent.Progress) {
                    progressEvents.add(it)
                }
            }
        }

        // when
        manager.startNextExercise()
        testScheduler.advanceUntilIdle()

        // then
        assertThat(progressEvents).isNotEmpty()
        job.cancel()
    }
}
