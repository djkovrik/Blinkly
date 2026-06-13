@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.achievements.integration

import com.sedsoftware.blinkly.component.achievements.AchievementsComponent.AchievementItem
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent.Model
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent.Section
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStore.State
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementLevel

internal val stateToModel: (State) -> Model = { state ->
    val items = state.achievements.map { it.toItem() }

    Model(
        sections = items
            .groupBy { it.level }
            .entries
            .sortedBy { it.key.index }
            .map { (level, achievements) ->
                Section(
                    level = level,
                    achievements = achievements.sortedBy { it.type.index },
                )
            },
        selectedAchievement = items.firstOrNull { it.type == state.selectedType && it.isDetailsAvailable },
    )
}

private fun Achievement.toItem(): AchievementItem {
    val unlocked = unlockedAt != null
    return AchievementItem(
        type = type,
        level = level,
        unlockedAt = unlockedAt,
        isUnlocked = unlocked,
        isDetailsAvailable = unlocked,
        isDescriptionHidden = !unlocked && level == AchievementLevel.HIDDEN,
    )
}
