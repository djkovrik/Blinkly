package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseType

/**
 * Achievement #18
 * 30 Exercises - Complete a total of 30 any exercises
 */
internal class ThirtyExercises : UnlockableAchievement {

    override val type: AchievementType = AchievementType.THIRTY_EXERCISES

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val count = calendar.flatMap { it.exercises }.size
        return count >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 30
    }
}
