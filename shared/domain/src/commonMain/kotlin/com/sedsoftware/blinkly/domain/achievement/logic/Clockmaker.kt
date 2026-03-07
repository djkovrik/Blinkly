package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseType

/**
 * Achievement #12
 * Clockmaker - Do the "Clock" eye rolls 50 times
 */
internal class Clockmaker : UnlockableAchievement {

    override val type: AchievementType = AchievementType.CLOCKMAKER

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val count = calendar.flatMap { it.exercises }.count { it.type == ExerciseType.CLOCK_ROLLS }
        return count >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 50
    }
}
