package com.sedsoftware.blinkly.domain.achievement

import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import com.sedsoftware.blinkly.domain.model.AchievementType

interface UnlockableAchievement {
    val type: AchievementType

    fun unlocked(achievements: List<Achievement>, calendar: List<Workout>): Boolean
}
