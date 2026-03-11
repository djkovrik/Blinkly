package com.sedsoftware.blinkly.domain.exercise

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import com.sedsoftware.blinkly.domain.base.BaseDomainTest
import com.sedsoftware.blinkly.domain.exercise.dsl.Blink
import com.sedsoftware.blinkly.domain.exercise.dsl.CompleteExerciseNode
import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseNode
import com.sedsoftware.blinkly.domain.exercise.dsl.MovementNode
import com.sedsoftware.blinkly.domain.exercise.dsl.exercise
import com.sedsoftware.blinkly.domain.exercise.engine.ExerciseEngine
import com.sedsoftware.blinkly.domain.extension.ms
import com.sedsoftware.blinkly.domain.model.ExerciseProgress
import com.sedsoftware.blinkly.domain.model.ExerciseType
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ExerciseEngineTest : BaseDomainTest() {

    private val engine: ExerciseEngine = ExerciseEngine(testDispatchers)

    @Test
    fun `when script executed then nodes emitted in order`() = runTest(testScheduler) {
        // given
        val script = exercise(ExerciseType.BLINK_BREAK) {
            Blink(1) every 10.ms
            Blink(2) every 10.ms
            completeExercise()
        }

        val emitted: MutableList<ExerciseNode> = mutableListOf()

        // when
        engine.run(
            script = script,
            emitter = { node, _ ->
                emitted.add(node)
            },
            isPaused = { false },
        )

        // then
        assertThat(emitted.size).isEqualTo(3)
        assertThat(emitted[0] is MovementNode).isTrue()
        assertThat(emitted[1] is MovementNode).isTrue()
        assertThat(emitted[2] is CompleteExerciseNode).isTrue()
    }

    @Test
    fun `when script runs then progress emitted`() = runTest(testScheduler) {
        // given
        val script = exercise(ExerciseType.BLINK_BREAK) {
            Blink(1) every 10.ms
            Blink(2) every 10.ms
        }

        val progressEvents = mutableListOf<ExerciseProgress>()

        // when
        engine.run(
            script = script,
            emitter = { _, progress ->
                progress?.let { progressEvents.add(it) }
            },
            isPaused = { false },
        )

        // then
        assertThat(progressEvents).isNotEmpty()
    }

    @Test
    fun `when paused then execution waits`() = runTest(testScheduler) {
        // given
        val script = exercise(ExerciseType.BLINK_BREAK) {
            Blink(1) every 100.ms
            completeExercise()
        }

        val emitted = mutableListOf<ExerciseNode>()
        var paused = true

        val job = launch {

            engine.run(
                script = script,
                emitter = { node, _ ->
                    emitted.add(node)
                },
                isPaused = { paused }
            )
        }

        // when
        testScheduler.advanceTimeBy(1000)

        // then
        assertThat(emitted).isEmpty()

        // when
        paused = false
        testScheduler.advanceUntilIdle()

        // then
        assertThat(emitted).isNotEmpty()
        job.cancel()
    }
}
