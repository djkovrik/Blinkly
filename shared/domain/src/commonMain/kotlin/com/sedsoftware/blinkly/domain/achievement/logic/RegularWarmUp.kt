package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseBlock
import com.sedsoftware.blinkly.domain.model.ExerciseType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #15
 * Regular Warm-up - Complete Block A ("Workplace Warm-up") 10 times
 */
internal class RegularWarmUp : UnlockableAchievement {

    override val type: AchievementType = AchievementType.REGULAR_WARM_UP

    private val requiredExercises: Set<ExerciseType> = setOf(
        ExerciseType.BLINK_BREAK,
        ExerciseType.NEAR_FAR_FOCUS,
        ExerciseType.DIAGONAL_GAZES,
    )

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val completeBlockACount = calendar.count { workout ->
            val completedTypesInBlockA = workout.exercises
                .filter { it.block == ExerciseBlock.A }
                .map { it.type }
                .toSet()

            completedTypesInBlockA.containsAll(requiredExercises)
        }

        return completeBlockACount >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 10
    }
}
