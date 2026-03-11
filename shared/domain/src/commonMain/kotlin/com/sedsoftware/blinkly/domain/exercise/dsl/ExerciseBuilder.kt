package com.sedsoftware.blinkly.domain.exercise.dsl

import com.sedsoftware.blinkly.domain.model.DurationMs
import com.sedsoftware.blinkly.domain.model.EyeMovement

@ExerciseDsl
internal class ExerciseBuilder {

    internal val nodes: MutableList<ExerciseNode> = mutableListOf()

    fun move(movement: EyeMovement, duration: DurationMs) {
        nodes += MovementNode(movement, duration)
    }

    fun tick(second: Int, duration: DurationMs) {
        nodes += TickNode(second, duration)
    }

    fun completeExercise() {
        nodes += CompleteExerciseNode
    }

    fun completeBlock() {
        nodes += CompleteBlockNode
    }
}
