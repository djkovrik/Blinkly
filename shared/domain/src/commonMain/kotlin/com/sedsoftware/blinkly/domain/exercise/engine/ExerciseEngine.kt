package com.sedsoftware.blinkly.domain.exercise.engine

import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseNode
import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.MovementNode
import com.sedsoftware.blinkly.domain.exercise.dsl.TickNode
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.model.ExerciseProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

internal class ExerciseEngine(
    private val dispatchers: BlinklyDispatchers,
) {

    suspend fun run(
        script: ExerciseScript,
        emitter: suspend (ExerciseNode, ExerciseProgress?) -> Unit,
        isPaused: () -> Boolean,
    ) = withContext(dispatchers.io) {

        val progressCalculator = ProgressCalculator(script)

        for (node in script.nodes) {

            while (isPaused()) {
                delay(PAUSE_CHECK_DELAY_MS)
            }

            val delayMs: Long = when (node) {
                is MovementNode -> node.delay.value
                is TickNode -> node.delay.value
                else -> 0
            }

            val progress: ExerciseProgress? =
                if (delayMs > 0)
                    progressCalculator.step(delayMs)
                else null

            emitter(node, progress)

            if (delayMs > 0) {
                delay(delayMs)
            }
        }
    }

    private companion object {
        const val PAUSE_CHECK_DELAY_MS = 100L
    }
}
