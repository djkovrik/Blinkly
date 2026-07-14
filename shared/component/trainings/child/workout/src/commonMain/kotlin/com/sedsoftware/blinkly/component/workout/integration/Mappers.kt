package com.sedsoftware.blinkly.component.workout.integration

import com.sedsoftware.blinkly.component.workout.WorkoutComponent
import com.sedsoftware.blinkly.component.workout.store.WorkoutStore
import com.sedsoftware.blinkly.domain.model.ExerciseProgress

internal val stateToModel: (WorkoutStore.State) -> WorkoutComponent.Model = { state ->
    WorkoutComponent.Model(
        block = state.block,
        exercises = state.exercises,
        currentExercise = state.exercises.getOrNull(state.currentExerciseIndex),
        currentExerciseIndex = state.currentExerciseIndex,
        phase = state.phase.toModelPhase(),
        movement = state.movement,
        movementDurationMs = state.movementDurationMs,
        movementTrigger = state.movementTrigger,
        progress = state.progress?.toModelProgress(),
        timerRemainingSeconds = state.timerRemainingSeconds(),
    )
}

private fun WorkoutStore.Phase.toModelPhase(): WorkoutComponent.Phase =
    when (this) {
        WorkoutStore.Phase.INTRO -> WorkoutComponent.Phase.INTRO
        WorkoutStore.Phase.READY -> WorkoutComponent.Phase.READY
        WorkoutStore.Phase.RUNNING -> WorkoutComponent.Phase.RUNNING
        WorkoutStore.Phase.PAUSED -> WorkoutComponent.Phase.PAUSED
        WorkoutStore.Phase.COMPLETED -> WorkoutComponent.Phase.COMPLETED
    }

private fun ExerciseProgress.toModelProgress(): WorkoutComponent.Progress =
    WorkoutComponent.Progress(
        percent = percent,
        remainingMs = remainingMs,
        totalMs = totalMs,
    )

private fun WorkoutStore.State.timerRemainingSeconds(): Int? {
    val elapsedSeconds = timerElapsedSeconds ?: return null
    val totalSeconds = progress?.totalMs?.div(SECOND_MS)?.toInt() ?: return null

    return (totalSeconds - elapsedSeconds).coerceAtLeast(0)
}

private const val SECOND_MS = 1_000L
