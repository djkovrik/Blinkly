package com.sedsoftware.blinkly.component.main

import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.main.domain.model.GreetingPeriod
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree

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
    )
}
