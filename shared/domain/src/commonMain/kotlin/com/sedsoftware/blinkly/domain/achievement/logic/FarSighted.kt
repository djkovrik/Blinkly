package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseType

/**
 * Achievement #10
 * Far-Sighted - Complete the 20-20-20 rule 100 times
 */
internal class FarSighted : UnlockableAchievement {

    override val type: AchievementType = AchievementType.FAR_SIGHTED

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val count = calendar.flatMap { it.exercises }.count { it.type == ExerciseType.TWENTY_X3 }
        return count >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 100
    }
}
