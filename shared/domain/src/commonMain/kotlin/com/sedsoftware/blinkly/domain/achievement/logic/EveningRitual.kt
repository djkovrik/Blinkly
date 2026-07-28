package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #16
 * Evening Ritual - Complete Block B ("Relax & Unwind") 10 times
 */
internal class EveningRitual : UnlockableAchievement {

    override val type: AchievementType = AchievementType.EVENING_RITUAL

    private val requiredExercises: Set<ExerciseType> = setOf(
        ExerciseType.FIGURE_EIGHT,
        ExerciseType.CLOCK_ROLLS,
        ExerciseType.PALMING,
    )

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val completeBlockBCount = calendar.sumOf { workout ->
            requiredExercises.minOf { requiredType ->
                workout.exercises.count { exercise ->
                    exercise.block == ExerciseBlock.B && exercise.type == requiredType
                }
            }
        }

        return completeBlockBCount >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 10
    }
}
