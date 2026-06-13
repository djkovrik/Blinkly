@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.sedsoftware.blinkly.component.achievements

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.domain.model.AchievementLevel
import com.sedsoftware.blinkly.domain.model.AchievementType
import kotlin.time.Instant

interface AchievementsComponent {

    val model: Value<Model>

    fun onBackClick()
    fun onAchievementClick(type: AchievementType)
    fun onDetailsDismiss()

    data class Model(
        val sections: List<Section>,
        val selectedAchievement: AchievementItem?,
    )

    data class Section(
        val level: AchievementLevel,
        val achievements: List<AchievementItem>,
    )

    data class AchievementItem(
        val type: AchievementType,
        val level: AchievementLevel,
        val unlockedAt: Instant?,
        val isUnlocked: Boolean,
        val isDetailsAvailable: Boolean,
        val isDescriptionHidden: Boolean,
    )
}
