package com.sedsoftware.blinkly.domain.exercise.engine

import com.sedsoftware.blinkly.domain.exercise.dsl.ExerciseScript
import com.sedsoftware.blinkly.domain.exercise.dsl.MovementNode
import com.sedsoftware.blinkly.domain.exercise.dsl.TickNode
import com.sedsoftware.blinkly.domain.model.ExerciseProgress

internal class ProgressCalculator(script: ExerciseScript) {

    private val totalDurationMs: Long =
        script.nodes.sumOf {
            when (it) {
                is MovementNode -> it.delay.value
                is TickNode -> it.delay.value
                else -> 0
            }
        }

    private var elapsed = 0L

    fun step(delay: Long): ExerciseProgress {

        elapsed += delay

        val percent: Int =
            if (totalDurationMs == 0L) 100
            else ((elapsed.toDouble() / totalDurationMs) * 100)
                .toInt()
                .coerceAtMost(100)

        return ExerciseProgress(
            percent = percent,
            remainingMs = (totalDurationMs - elapsed).coerceAtLeast(0),
            totalMs = totalDurationMs
        )
    }
}
