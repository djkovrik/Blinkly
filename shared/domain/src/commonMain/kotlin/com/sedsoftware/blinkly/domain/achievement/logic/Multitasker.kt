package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #37
 * Multitasker - Complete five different exercises on ten different days
 */
internal class Multitasker : UnlockableAchievement {

    override val type: AchievementType = AchievementType.MULTITASKER

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val differentExercisesDaysCount = calendar.count { workout ->
            val count = workout.exercises.map { it.type }.toSet()
            count.size >= EXERCISES_THRESHOLD
        }

        return differentExercisesDaysCount >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val EXERCISES_THRESHOLD = 5
        const val ACHIEVEMENT_THRESHOLD = 10
    }
}
