package com.sedsoftware.blinkly.component.main.integration

import com.sedsoftware.blinkly.component.main.MainTabComponent.Model
import com.sedsoftware.blinkly.component.main.store.MainTabStore.State

internal val stateToModel: (State) -> Model = {
    Model(
        greetingPeriod = it.greetingPeriod,
        restMinutesToday = it.restMinutesToday,
        exercisesToday = it.exercisesToday,
        twentyX3Today = it.twentyX3Today,
        palmingToday = it.palmingToday,
        dailyProgressPercent = it.dailyProgressPercent,
        tree = it.tree,
        treeGrowthStreakDays = it.treeGrowthStreakDays,
        highlight = it.highlight,
        ctaState = it.ctaState,
    )
}
