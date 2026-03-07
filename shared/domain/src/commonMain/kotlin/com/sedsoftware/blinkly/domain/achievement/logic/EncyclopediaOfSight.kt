package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.extension.getLevel
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #33
 * Encyclopedia of Sight - Unlock all regular achievements
 */
internal class EncyclopediaOfSight : UnlockableAchievement {

    override val type: AchievementType = AchievementType.ENCYCLOPEDIA_OF_SIGHT

    private val requiredLevels: Set<AchievementLevel> = setOf(
        AchievementLevel.BEGINNER,
        AchievementLevel.INTERMEDIATE,
        AchievementLevel.PRO,
        AchievementLevel.EXPERT,
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
