package com.sedsoftware.blinkly.database.mapper

import com.sedsoftware.blinkly.database.AchievementEntity
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType

internal object AchievementMapper {
    fun toDomain(from: List<AchievementEntity>): List<Achievement> =
        from.map { item: AchievementEntity ->
            Achievement(
                type = AchievementType.fromIndex(item.type),
                level = AchievementLevel.fromIndex(item.level),
                unlockedAt = item.unlockedAt,
            )
        }
}
