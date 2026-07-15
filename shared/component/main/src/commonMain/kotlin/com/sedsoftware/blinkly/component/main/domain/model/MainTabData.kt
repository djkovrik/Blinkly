package com.sedsoftware.blinkly.component.main.domain.model

import kotlin.time.Duration

internal data class MainTabData(
    val greetingPeriod: GreetingPeriod,
    val restMinutesToday: Int,
    val exercisesToday: Int,
    val twentyX3Today: Int,
    val palmingToday: Int,
    val dailyProgressPercent: Int,
    val treeGrowthStreakDays: Int,
    val ctaState: MainCtaState,
    val ctaRefreshAfter: Duration?,
)
