package com.sedsoftware.blinkly.component.main.integration

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.sedsoftware.blinkly.component.main.MainTabComponent
import com.sedsoftware.blinkly.component.main.MainTabComponent.Model
import com.sedsoftware.blinkly.component.main.domain.model.GreetingPeriod
import com.sedsoftware.blinkly.component.main.domain.model.MainCtaState
import com.sedsoftware.blinkly.domain.model.HighlightOfTheDay
import com.sedsoftware.blinkly.domain.model.Tree

class MainTabComponentPreview(
    private val greetingPeriod: GreetingPeriod = GreetingPeriod.DAY,
    private val restMinutesToday: Int = 0,
    private val exercisesToday: Int = 0,
    private val twentyX3Today: Int = 0,
    private val palmingToday: Int = 0,
    private val dailyProgressPercent: Int = 0,
    private val tree: Tree? = null,
    private val treeGrowthStreakDays: Int = 0,
    private val highlight: HighlightOfTheDay? = null,
    private val ctaState: MainCtaState = MainCtaState.Idle,
) : MainTabComponent {

    override val model: Value<Model> = MutableValue(
        Model(
            greetingPeriod = greetingPeriod,
            restMinutesToday = restMinutesToday,
            exercisesToday = exercisesToday,
            twentyX3Today = twentyX3Today,
            palmingToday = palmingToday,
            dailyProgressPercent = dailyProgressPercent,
            tree = tree,
            treeGrowthStreakDays = treeGrowthStreakDays,
            highlight = highlight,
            ctaState = ctaState,
        )
    )

    override fun onPreferencesClick() = Unit
    override fun onTreeClick() = Unit
    override fun onPrimaryCtaClick() = Unit
}
