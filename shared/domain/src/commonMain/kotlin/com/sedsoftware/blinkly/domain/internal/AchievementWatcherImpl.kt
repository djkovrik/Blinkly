package com.sedsoftware.blinkly.domain.internal

import com.sedsoftware.blinkly.domain.AchievementWatcher
import com.sedsoftware.blinkly.domain.external.BlinklyDispatchers
import com.sedsoftware.blinkly.domain.achievement.UnlockableAchievement
import com.sedsoftware.blinkly.domain.achievement.logic.BlinkStarter
import com.sedsoftware.blinkly.domain.achievement.logic.FirstSpark
import com.sedsoftware.blinkly.domain.external.BlinklyDatabase
import com.sedsoftware.blinkly.domain.external.BlinklyNotifier
import com.sedsoftware.blinkly.domain.external.BlinklySettings
import com.sedsoftware.blinkly.domain.model.Achievement
import com.sedsoftware.blinkly.domain.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class AchievementWatcherImpl(
    private val database: BlinklyDatabase,
    private val notifier: BlinklyNotifier,
    private val settings: BlinklySettings,
    dispatchers: BlinklyDispatchers,
) : AchievementWatcher {

    private val instances: List<UnlockableAchievement> = registerAchievements()
    private val scope: CoroutineScope = CoroutineScope(dispatchers.io)

    private var observeAchievementsJob: Job? = null
    private var observeCalendarJob: Job? = null

    private var latestAchievements: List<Achievement> = emptyList()
    private var latestCalendar: List<Workout> = emptyList()

    override fun start() {
        observeAchievementsJob?.cancel()
        observeAchievementsJob = scope.launch {
            database.currentAchievements().collect { achievements: List<Achievement> ->
                latestAchievements = achievements
                checkIfUnlocked()
            }
        }

        observeCalendarJob?.cancel()
        observeCalendarJob = scope.launch {
            database.currentCalendar().collect { calendar: List<Workout> ->
                latestCalendar = calendar
                checkIfUnlocked()
            }
        }
    }

    override fun stop() {
        observeCalendarJob?.cancel()
        observeAchievementsJob?.cancel()
        scope.cancel()
    }

    private fun checkIfUnlocked() {
        instances.forEach { instance: UnlockableAchievement ->
            if (instance.unlocked(latestAchievements, latestCalendar)) {
                notifier.achievementUnlocked(instance.type)
            }
        }
    }

    private fun registerAchievements(): List<UnlockableAchievement> = listOf(
        FirstSpark(),
        BlinkStarter(settings.blinkBreakCount),
    )
}
