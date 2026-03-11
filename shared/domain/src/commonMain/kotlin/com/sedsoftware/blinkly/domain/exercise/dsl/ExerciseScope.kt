package com.sedsoftware.blinkly.domain.exercise.dsl

import com.sedsoftware.blinkly.domain.extension.seconds
import com.sedsoftware.blinkly.domain.model.DurationMs
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.EyeMovement

@ExerciseDsl
internal class ExerciseScope(
    private val builder: ExerciseBuilder,
) {

    infix fun EyeMovement.every(duration: DurationMs) {
        builder.move(this, duration)
    }

    fun completeExercise() {
        builder.completeExercise()
    }

    fun completeBlock() {
        builder.completeBlock()
    }

    fun repeat(times: Int, block: ExerciseScope.(Int) -> Unit) {
        for (i in 1..times) {
            block(i)
        }
    }

    fun timer(
        duration: DurationMs,
        block: (second: Int) -> Unit = {},
    ) {
        val seconds = duration.value / 1000

        for (s in 1..seconds.toInt()) {
            block(s)
            builder.tick(s, 1.seconds)
        }
    }
}

internal fun exercise(
    type: ExerciseType,
    block: ExerciseScope.() -> Unit,
): ExerciseScript {

    val builder = ExerciseBuilder()
    val scope = ExerciseScope(builder)

    scope.block()

    return ExerciseScript(type, builder.nodes)
}
