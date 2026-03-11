package com.sedsoftware.blinkly.domain.exercise.dsl

import com.sedsoftware.blinkly.domain.model.DurationMs
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.EyeMovement

internal class ExerciseScript(
    val type: ExerciseType,
    val nodes: List<ExerciseNode>,
)

internal sealed interface ExerciseNode

internal data class MovementNode(
    val movement: EyeMovement,
    val delay: DurationMs,
) : ExerciseNode

internal data class TickNode(
    val second: Int,
    val delay: DurationMs,
) : ExerciseNode

internal data object CompleteExerciseNode : ExerciseNode

internal data object CompleteBlockNode : ExerciseNode
