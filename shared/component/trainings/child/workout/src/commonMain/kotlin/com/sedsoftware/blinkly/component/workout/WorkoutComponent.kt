package com.sedsoftware.blinkly.component.workout

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.EyeMovement

interface WorkoutComponent {
    val model: Value<Model>

    fun onBackClick()
    fun onStartClick()
    fun onPauseClick()
    fun onResumeClick()
    fun onFinishClick()

    data class Model(
        val block: ExerciseBlock,
        val exercises: List<ExerciseType>,
        val currentExercise: ExerciseType?,
        val currentExerciseIndex: Int,
        val phase: Phase,
        val movement: EyeMovement?,
        val movementTrigger: Int,
        val progress: Progress?,
        val timerRemainingSeconds: Int?,
    )

    data class Progress(
        val percent: Int,
        val remainingMs: Long,
        val totalMs: Long,
    )

    enum class Phase {
        INTRO,
        READY,
        RUNNING,
        PAUSED,
        COMPLETED,
    }
}
