package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.extension.hasNConsecutiveDays
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.datetime.TimeZone

/**
 * Achievement #20
 * Iron Gaze - Exercise for 30 days in a row
 */
internal class IronGaze(
    private val timeZone: TimeZone,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.IRON_GAZE

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.hasNConsecutiveDays(ACHIEVEMENT_THRESHOLD, timeZone)
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 30
    }
}
