package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.ExerciseType

/**
 * Achievement #5
 * Evening Newbie - Spend a total of 5 minutes in Palming
 */
internal class EveningNewbie(
    private val palmingDuration: Int,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.EVENING_NEWBIE

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val count = calendar.flatMap { it.exercises }.count { it.type == ExerciseType.PALMING }
        val result = count * palmingDuration
        return result >= ACHIEVEMENT_THRESHOLD
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 300
    }
}
