package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #38
 * Yin Yang - Complete any workout after switching the app theme from dark to light, and vice versa.
 */
internal class YinYang(
    private val lightThemeCompleted: () -> Boolean,
    private val darkThemeCompleted: () -> Boolean,
) : UnlockableAchievement {

    override val type: AchievementType = AchievementType.YIN_YANG

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        return lightThemeCompleted.invoke() && darkThemeCompleted.invoke()
    }
}
