@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.trainings.domain

import com.sedsoftware.blinkly.domain.BlinklyCalendarWatcher
import com.sedsoftware.blinkly.domain.extension.asLocalDate
import com.sedsoftware.blinkly.domain.external.BlinklyTimeUtils
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.toLocalDateTime

internal class TrainingsTabManager(
    private val calendarWatcher: BlinklyCalendarWatcher,
    private val timeUtils: BlinklyTimeUtils,
) {

    val calendar: Flow<List<Workout>>
        get() = calendarWatcher.calendar

    fun completedToday(calendar: List<Workout>): Result<Set<ExerciseBlock>> = runCatching {
        val timeZone = timeUtils.timeZone()
        val today = timeUtils.now().toLocalDateTime(timeZone).date

        val todayExercises = calendar
            .flatMap { workout -> workout.exercises }
            .filter { exercise -> exercise.completedAt.asLocalDate(timeZone) == today }

        buildSet {
            if (todayExercises.isBlockACompleted()) {
                add(ExerciseBlock.A)
            }
            if (todayExercises.isBlockBCompleted()) {
                add(ExerciseBlock.B)
            }
            if (todayExercises.any { it.type == ExerciseType.TWENTY_X3 }) {
                add(ExerciseBlock.C)
            }
        }
    }

    private fun List<Exercise>.isBlockACompleted(): Boolean =
        filter { it.block == ExerciseBlock.A }
            .map { it.type }
            .containsAll(BLOCK_A_TYPES)

    private fun List<Exercise>.isBlockBCompleted(): Boolean =
        filter { it.block == ExerciseBlock.B }
            .map { it.type }
            .containsAll(BLOCK_B_TYPES)

    private companion object {
        val BLOCK_A_TYPES: Set<ExerciseType> = setOf(
            ExerciseType.BLINK_BREAK,
            ExerciseType.NEAR_FAR_FOCUS,
            ExerciseType.DIAGONAL_GAZES,
        )
        val BLOCK_B_TYPES: Set<ExerciseType> = setOf(
            ExerciseType.FIGURE_EIGHT,
            ExerciseType.CLOCK_ROLLS,
            ExerciseType.PALMING,
        )
    }
}
