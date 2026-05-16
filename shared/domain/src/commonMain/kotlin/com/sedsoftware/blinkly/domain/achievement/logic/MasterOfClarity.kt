package com.sedsoftware.blinkly.domain.achievement.logic

import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType
import com.sedsoftware.blinkly.domain.model.Workout

/**
 * Achievement #34
 * Master Of Clarity - Unlock all achievements including hidden ones
 */
internal class MasterOfClarity : UnlockableAchievement {

    override val type: AchievementType = AchievementType.MASTER_OF_CLARITY

    override fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean {
        val unlockedTypes = achievements
            .map { it.type }
            .toSet()

        val requiredTypes = AchievementType.entries
            .filter { it != AchievementType.MASTER_OF_CLARITY }
            .toSet()

        return unlockedTypes.containsAll(requiredTypes)
    }
}
