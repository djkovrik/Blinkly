package com.sedsoftware.blinkly.domain.achievement

import com.sedsoftware.blinkly.domain.model.Achievement
import kotlin.time.Clock
import kotlin.time.Instant

abstract class BaseAchievementTest {

    abstract val achievement: UnlockableAchievement

    protected val now: Instant
        get() = Clock.System.now()

    protected val emptyAchievements: List<Achievement>
        get() = emptyList()
}
