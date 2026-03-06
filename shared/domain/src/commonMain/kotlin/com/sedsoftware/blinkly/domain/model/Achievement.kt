package com.sedsoftware.blinkly.domain.model

import kotlin.time.Instant

data class Achievement(
    val type: AchievementType,
    val level: AchievementLevel,
    val unlockedAt: Instant?,
)
