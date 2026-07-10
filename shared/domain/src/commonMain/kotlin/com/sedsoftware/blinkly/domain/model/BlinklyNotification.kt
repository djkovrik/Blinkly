package com.sedsoftware.blinkly.domain.model

sealed class BlinklyNotification {

    data class AchievementUnlocked(val type: AchievementType) : BlinklyNotification()
}
