@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.achievements.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent.AchievementItem
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent.Model
import com.sedsoftware.blinkly.component.achievements.AchievementsComponent.Section
import com.sedsoftware.blinkly.domain.extension.getLevel
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import kotlin.time.Instant

class AchievementsComponentPreview(
    achievements: List<AchievementItem> = previewAchievements(),
    selectedAchievement: AchievementItem? = null,
) : AchievementsComponent {

    override val model: Value<Model> = MutableValue(
        Model(
            sections = achievements
                .groupBy { it.level }
                .entries
                .sortedBy { it.key.index }
                .map { (level, items) -> Section(level, items) },
            selectedAchievement = selectedAchievement,
        )
    )

    override fun onBackClick() = Unit
    override fun onAchievementClick(type: AchievementType) = Unit
    override fun onDetailsDismiss() = Unit
}

private fun previewAchievements(): List<AchievementItem> =
    listOf(
        previewItem(AchievementType.FIRST_SPARK, unlockedAt = Instant.parse("2026-03-14T09:15:00Z")),
        previewItem(AchievementType.BLINK_STARTER),
        previewItem(AchievementType.DIAMOND_EYES, unlockedAt = Instant.parse("2026-03-15T18:30:00Z")),
        previewItem(AchievementType.FAR_SIGHTED),
        previewItem(AchievementType.IRON_GAZE),
        previewItem(AchievementType.FALCON_EYE, unlockedAt = Instant.parse("2026-03-16T07:45:00Z")),
        previewItem(AchievementType.EARLY_BIRD),
        previewItem(AchievementType.THINK_TANK, unlockedAt = Instant.parse("2026-03-17T20:05:00Z")),
    )

private fun previewItem(
    type: AchievementType,
    unlockedAt: Instant? = null,
): AchievementItem {
    val level = type.getLevel()
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
