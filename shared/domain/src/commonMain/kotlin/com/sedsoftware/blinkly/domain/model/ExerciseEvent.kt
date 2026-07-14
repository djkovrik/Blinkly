package com.sedsoftware.blinkly.domain.model

sealed interface ExerciseEvent {
    val block: ExerciseBlock
    val exercise: ExerciseType?

    data class Movement(
        override val block: ExerciseBlock,
        override val exercise: ExerciseType,
        val movement: EyeMovement,
        val durationMs: Long,
    ) : ExerciseEvent

    data class Tick(
        override val block: ExerciseBlock,
        override val exercise: ExerciseType,
        val second: Int,
    ) : ExerciseEvent

    data class Progress(
        override val block: ExerciseBlock,
        override val exercise: ExerciseType,
        val progress: ExerciseProgress,
    ) : ExerciseEvent

    data class Beep(
        override val block: ExerciseBlock,
        override val exercise: ExerciseType,
    ) : ExerciseEvent

    data class ExerciseCompleted(
        override val block: ExerciseBlock,
        override val exercise: ExerciseType,
    ) : ExerciseEvent

    data class BlockCompleted(
        override val block: ExerciseBlock,
        override val exercise: ExerciseType? = null,
    ) : ExerciseEvent

    data class Error(
        override val block: ExerciseBlock,
        override val exercise: ExerciseType?,
        val throwable: Throwable,
    ) : ExerciseEvent
}
