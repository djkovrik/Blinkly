package com.sedsoftware.blinkly.database.mapper

import com.sedsoftware.blinkly.database.AchievementEntity
import com.sedsoftware.blinkly.domain.model.Achievement

internal object AchievementMapper {
    fun toDomain(from: List<AchievementEntity>): List<Achievement> =
        from.map { item: AchievementEntity ->
            Achievement(
                type = item.type,
                level = item.level,
                unlockedAt = item.unlockedAt,
            )
        }
}
