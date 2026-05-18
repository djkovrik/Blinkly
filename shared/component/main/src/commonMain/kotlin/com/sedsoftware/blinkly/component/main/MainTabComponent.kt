package com.sedsoftware.blinkly.component.main

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree
import com.sedsoftware.blinkly.domain.model.TreeStage
import com.sedsoftware.blinkly.domain.model.TreeType

interface MainTabComponent {

    val model: Value<Model>

    fun onPreferencesClick()
    fun onTreeClick()
    fun onPrimaryCtaClick()

    data class Model(
        val greetingPeriod: GreetingPeriod,
        val restMinutesToday: Int,
        val exercisesToday: Int,
        val twentyX3Today: Int,
        val palmingToday: Int,
        val dailyProgressPercent: Int,
        val tree: Tree,
        val treeGrowthStreakDays: Int,
        val highlight: HighlightOfTheDay?,
        val ctaState: MainCtaState,
    ) {
        companion object {
            val EMPTY: Model =
                Model(
                    greetingPeriod = GreetingPeriod.DAY,
                    restMinutesToday = 0,
                    exercisesToday = 0,
                    twentyX3Today = 0,
                    palmingToday = 0,
                    dailyProgressPercent = 0,
                    tree = Tree(TreeStage.TINY, TreeType.FRAXINUS_EXCELSIOR, 0f),
                    treeGrowthStreakDays = 0,
                    highlight = null,
                    ctaState = MainCtaState.Idle,
                )
        }
    }

    enum class GreetingPeriod {
        MORNING,
        DAY,
        EVENING,
    }

    sealed class MainCtaState {
        data object MorningWarmUp : MainCtaState()
        data object WorkBreakDue : MainCtaState()
        data object AfternoonWarmUp : MainCtaState()
        data object EveningRelax : MainCtaState()
        data object DayClosing : MainCtaState()
        data object PerfectDay : MainCtaState()
        data object Idle : MainCtaState()
    }
}
