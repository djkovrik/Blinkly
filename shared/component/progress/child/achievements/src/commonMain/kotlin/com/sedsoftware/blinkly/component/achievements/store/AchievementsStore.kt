package com.sedsoftware.blinkly.component.achievements.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStore.Intent
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStore.Label
import com.sedsoftware.blinkly.component.achievements.store.AchievementsStore.State
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.AchievementType

internal interface AchievementsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class AchievementClicked(val type: AchievementType) : Intent
        data object DetailsDismissed : Intent
    }

    data class State(
        val achievements: List<Achievement> = emptyList(),
        val selectedType: AchievementType? = null,
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }
}
