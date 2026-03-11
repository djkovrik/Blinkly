package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Exercise
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #25
 * The All-Seeing - Complete all three blocks (A, B, and C) within a single day, and do this on seven different days
 */
internal class TheAllSeeing : UnlockableAchievement {

    override val type: AchievementType = AchievementType.THE_ALL_SEEING

    private val exerciseTypesBlockA: Set<ExerciseType> = setOf(
        ExerciseType.BLINK_BREAK,
        ExerciseType.NEAR_FAR_FOCUS,
        ExerciseType.DIAGONAL_GAZES
    )

    private val exerciseTypesBlockB: Set<ExerciseType> = setOf(
        ExerciseType.FIGURE_EIGHT,
        ExerciseType.CLOCK_ROLLS,
        ExerciseType.PALMING
    )

    private val exerciseTypesBlockC: Set<ExerciseType> = setOf(
        ExerciseType.TWENTY_X3
    )

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val completeAllBlocksCount = calendar.count { workout ->
            val isBlockACompleted = isBlockCompleted(workout.exercises, ExerciseBlock.A, exerciseTypesBlockA)
            val isBlockBCompleted = isBlockCompleted(workout.exercises, ExerciseBlock.B, exerciseTypesBlockB)
            val isBlockCCompleted = isBlockCompleted(workout.exercises, ExerciseBlock.C, exerciseTypesBlockC)

            isBlockACompleted && isBlockBCompleted && isBlockCCompleted
        }

        return completeAllBlocksCount >= ACHIEVEMENT_THRESHOLD
    }

    private fun isBlockCompleted(exercises: List<Exercise>, block: ExerciseBlock, requiredTypes: Set<ExerciseType>): Boolean {
        val completedTypes = exercises.filter { it.block == block }.map { it.type }.toSet()
        return completedTypes.containsAll(requiredTypes)
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 7
    }
}
