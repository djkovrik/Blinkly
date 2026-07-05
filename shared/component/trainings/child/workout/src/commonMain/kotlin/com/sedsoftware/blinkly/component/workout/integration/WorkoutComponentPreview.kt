package com.sedsoftware.blinkly.component.workout.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.workout.WorkoutComponent
import com.sedsoftware.blinkly.component.workout.WorkoutComponent.Model
import com.sedsoftware.blinkly.component.workout.store.exercises
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.EyeMovement

class WorkoutComponentPreview(
    block: ExerciseBlock = ExerciseBlock.A,
    currentExercise: ExerciseType? = block.exercises().firstOrNull(),
    phase: WorkoutComponent.Phase = WorkoutComponent.Phase.INTRO,
    movement: EyeMovement? = null,
    movementTrigger: Int = 0,
    progress: WorkoutComponent.Progress? = null,
    timerRemainingSeconds: Int? = null,
) : WorkoutComponent {

    override val model: Value<Model> = MutableValue(
        Model(
            block = block,
            exercises = block.exercises(),
            currentExercise = currentExercise,
            currentExerciseIndex = block.exercises().indexOf(currentExercise).coerceAtLeast(0),
            phase = phase,
            movement = movement,
            movementTrigger = movementTrigger,
            progress = progress,
            timerRemainingSeconds = timerRemainingSeconds,
        )
    )

    override fun onBackClick() = Unit
    override fun onStartClick() = Unit
    override fun onPauseClick() = Unit
    override fun onResumeClick() = Unit
    override fun onFinishClick() = Unit
}
