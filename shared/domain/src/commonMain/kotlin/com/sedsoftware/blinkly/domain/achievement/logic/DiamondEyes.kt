package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.extension.hasNConsecutiveDays
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #8
 * Diamond Eyes - Exercise for 7 days in a row
 */
internal class DiamondEyes : UnlockableAchievement {

    override val type: AchievementType = AchievementType.DIAMOND_EYES

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.hasNConsecutiveDays(ACHIEVEMENT_THRESHOLD)
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 7
    }
}
