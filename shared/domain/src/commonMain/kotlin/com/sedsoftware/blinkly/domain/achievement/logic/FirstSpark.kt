package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.model.AchievementType

/**
 * Achievement #1
 * First Spark - Complete your first workout (any exercise)
 */
internal class FirstSpark : UnlockableAchievement {

    override val type: AchievementType = AchievementType.FIRST_SPARK

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.isNotEmpty()
    }
}
