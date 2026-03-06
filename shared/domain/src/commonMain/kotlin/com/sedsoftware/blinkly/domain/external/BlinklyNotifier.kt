package com.sedsoftware.blinkly.domain.external

import com.sedsoftware.blinkly.domain.model.AchievementType

interface BlinklyNotifier {
    fun achievementUnlocked(achievement: AchievementType)
}
