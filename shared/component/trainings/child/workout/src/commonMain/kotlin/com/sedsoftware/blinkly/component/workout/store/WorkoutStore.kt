package com.sedsoftware.blinkly.component.workout.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseProgress
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.EyeMovement

internal interface WorkoutStore : Store<WorkoutStore.Intent, WorkoutStore.State, WorkoutStore.Label> {

    sealed interface Intent {
        data object StartClicked : Intent
        data object PauseClicked : Intent
        data object ResumeClicked : Intent
    }

    data class State(
        val block: ExerciseBlock,
        val exercises: List<ExerciseType>,
        val currentExerciseIndex: Int = 0,
        val phase: Phase = Phase.INTRO,
        val movement: EyeMovement? = null,
        val movementTrigger: Int = 0,
        val progress: ExerciseProgress? = null,
        val timerElapsedSeconds: Int? = null,
    )

    sealed interface Label {
        data class ErrorCaught(val exception: Throwable) : Label
    }

    enum class Phase {
        INTRO,
        READY,
        RUNNING,
        PAUSED,
        COMPLETED,
    }
}
