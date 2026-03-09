package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.extension.getLevel
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #34
 * Master Of Clarity - Unlock all achievements including hidden ones
 */
internal class MasterOfClarity : UnlockableAchievement {

    override val type: AchievementType = AchievementType.MASTER_OF_CLARITY

    private val requiredLevels: Set<AchievementLevel> = setOf(
        AchievementLevel.BEGINNER,
        AchievementLevel.INTERMEDIATE,
        AchievementLevel.PRO,
        AchievementLevel.EXPERT,
        AchievementLevel.HIDDEN,
    )

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val unlockedTypes = achievements
            .map { it.type }
            .toSet()

        val requiredTypes = AchievementType.entries
            .filter { requiredLevels.contains(it.getLevel()) }
            .toSet()

        return unlockedTypes.containsAll(requiredTypes)
    }
}
