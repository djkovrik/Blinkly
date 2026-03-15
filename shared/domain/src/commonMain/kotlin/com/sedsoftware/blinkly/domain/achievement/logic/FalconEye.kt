package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.extension.hasNConsecutiveDays
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.datetime.TimeZone

/**
 * Achievement #27
 * Falcon Eye - Exercise for 100 days in a row
 */
internal class FalconEye(
    private val timeZone: TimeZone,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.FALCON_EYE

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return calendar.hasNConsecutiveDays(ACHIEVEMENT_THRESHOLD, timeZone)
    }

    private companion object {
        const val ACHIEVEMENT_THRESHOLD = 100
    }
}
