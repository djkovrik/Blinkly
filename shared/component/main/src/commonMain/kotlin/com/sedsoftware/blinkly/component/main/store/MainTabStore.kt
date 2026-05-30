package com.sedsoftware.blinkly.component.main.store

import com.arkivanov.mvikotlin.core.store.Store
import com.sedsoftware.blinkly.component.main.domain.model.GreetingPeriod
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.component.main.store.MainTabStore.Intent
import com.sedsoftware.blinkly.component.main.store.MainTabStore.Label
import com.sedsoftware.blinkly.component.main.store.MainTabStore.State
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType
import com.sedsoftware.blinkly.domain.model.Workout

internal interface MainTabStore : Store<Intent, State, Label> {

    sealed interface Intent

    data class State(
        val calendar: List<Workout> = emptyList(),
        val tree: Tree? = null,
        val highlight: HighlightOfTheDay? = null,
        val greetingPeriod: GreetingPeriod = GreetingPeriod.DAY,
        val restMinutesToday: Int = 0,
        val exercisesToday: Int = 0,
        val twentyX3Today: Int = 0,
        val palmingToday: Int = 0,
        val dailyProgressPercent: Int = 0,
        val treeGrowthStreakDays: Int = 0,
        val ctaState: MainCtaState = MainCtaState.Idle,
    )

    sealed class Label {
        data class ErrorCaught(val exception: Throwable) : Label()
    }
}
