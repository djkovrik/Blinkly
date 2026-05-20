package com.sedsoftware.blinkly.component.main.domain.model

internal data class MainTabData(
    val greetingPeriod: GreetingPeriod,
    val restMinutesToday: Int,
    val exercisesToday: Int,
    val twentyX3Today: Int,
    val palmingToday: Int,
    val dailyProgressPercent: Int,
    val treeGrowthStreakDays: Int,
    val ctaState: MainCtaState,
)
